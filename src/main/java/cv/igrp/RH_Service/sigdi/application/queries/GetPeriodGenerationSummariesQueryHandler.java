package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationSummaryDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Contagens de geração de formulários em bloco, para vários períodos numa só chamada (Fase 119,
 * {@code PRZ-05}/{@code PRZ-07}) -- a coluna da tabela do ecrã (D-24, 119-05-PLAN.md). A leitura
 * completa de um único período é {@link GetPeriodGenerationQueryHandler}; este handler reutiliza
 * exactamente o mesmo critério de escolha de lote
 * ({@link GetPeriodGenerationQueryHandler#selectMostRecentNonDryRun}, D-28) -- duas regras que
 * divergissem fariam a coluna e o modal contradizerem-se.
 *
 * <p>{@link FormGenerationBatchRepository#findByPeriodIds} é chamado exactamente uma vez, com
 * todos os identificadores pedidos -- nunca um pedido por período, que seria N+1 sobre uma
 * página de dez.
 */
@Component
public class GetPeriodGenerationSummariesQueryHandler
        implements QueryHandler<GetPeriodGenerationSummariesQuery, ResponseEntity<List<FormGenerationSummaryDTO>>> {

    // T-119-23: limite de identificadores por pedido, para que o parâmetro de consulta não seja
    // um varrimento da tabela inteira.
    static final int MAX_PERIOD_IDS = 100;

    private final FormGenerationBatchRepository batchRepository;

    public GetPeriodGenerationSummariesQueryHandler(FormGenerationBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @IgrpQueryHandler
    @Override
    public ResponseEntity<List<FormGenerationSummaryDTO>> handle(GetPeriodGenerationSummariesQuery query) {
        List<String> rawIds = query.getPeriodIds() != null ? query.getPeriodIds() : List.of();

        if (rawIds.size() > MAX_PERIOD_IDS) {
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível consultar mais de " + MAX_PERIOD_IDS
                            + " períodos de uma só vez; recebidos " + rawIds.size() + ".");
        }

        // LinkedHashSet: um identificador repetido na entrada aparece uma única vez na saída,
        // preservando a ordem de primeira ocorrência.
        Set<UUID> periodIds = new LinkedHashSet<>();
        for (String rawId : rawIds) {
            try {
                periodIds.add(UUID.fromString(rawId));
            } catch (IllegalArgumentException ex) {
                throw IgrpResponseStatusException.badRequest(
                        "Identificador de período inválido: " + rawId);
            }
        }

        if (periodIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<FormGenerationBatch> batches = batchRepository.findByPeriodIds(periodIds);
        Map<UUID, List<FormGenerationBatch>> byPeriod = batches.stream()
                .collect(Collectors.groupingBy(FormGenerationBatch::getPeriodId));

        List<FormGenerationSummaryDTO> summaries = new ArrayList<>();
        for (UUID periodId : periodIds) {
            summaries.add(toSummary(periodId, byPeriod.getOrDefault(periodId, List.of())));
        }
        return ResponseEntity.ok(summaries);
    }

    private static FormGenerationSummaryDTO toSummary(UUID periodId, List<FormGenerationBatch> periodBatches) {
        FormGenerationSummaryDTO dto = new FormGenerationSummaryDTO();
        dto.setPeriodId(periodId.toString());

        // FormGenerationBatchRepository.findByPeriodIds (plural) não tem contrato de ordem
        // documentado, ao contrário de findByPeriodId (singular) -- ordena-se aqui antes de
        // reutilizar o critério da Task 2, para que a escolha seja robusta mesmo que o
        // adaptador plural não devolva já por generatedAt descendente.
        List<FormGenerationBatch> sorted = periodBatches.stream()
                .sorted(Comparator.comparing(FormGenerationBatch::getGeneratedAt).reversed())
                .toList();

        Optional<FormGenerationBatch> selected =
                GetPeriodGenerationQueryHandler.selectMostRecentNonDryRun(sorted);

        if (selected.isEmpty()) {
            dto.setStatus("NOT_GENERATED");
            dto.setDryRun(false);
            return dto;
        }

        FormGenerationBatch batch = selected.get();
        dto.setStatus(batch.getStatus() != null ? batch.getStatus().getCode() : null);
        dto.setGenerationMode(batch.getGenerationMode());
        dto.setCreatedCount(batch.getCreatedCount());
        dto.setFailedCount(batch.getFailedCount());
        dto.setSkippedCount(batch.getSkippedCount());
        dto.setPendingCount(batch.getPendingCount());
        dto.setGeneratedAt(batch.getGeneratedAt());
        dto.setGeneratedBy(batch.getGeneratedBy());
        dto.setDryRun(batch.isDryRun());
        // Fase 120, plano 03 (PRZ-04): nulo/zero enquanto o lote não foi desfeito.
        dto.setRevertedAt(batch.getRevertedAt());
        dto.setRevertedBy(batch.getRevertedBy());
        dto.setRevertedCount(batch.getRevertedCount());
        dto.setRevertBlockedCount(batch.getRevertBlockedCount());
        return dto;
    }
}
