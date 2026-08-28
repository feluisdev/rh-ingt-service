package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationRevertResultDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationRevertSkippedItemDTO;
import cv.igrp.RH_Service.sigdi.application.service.FormGenerationBatchRevertService;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Desfaz um lote de geração de formulários (Fase 120, plano 03, {@code PRZ-04}): carrega o lote,
 * aplica as quatro guardas que recusam um pedido sem sentido, corre
 * {@link FormGenerationBatchRevertService#revert} sobre os itens, e persiste o resultado com
 * {@link FormGenerationBatchRepository#markReverted} -- por esta ordem, nunca a marcação antes da
 * remoção.
 *
 * <p><strong>As quatro guardas, todas antes de tocar em qualquer avaliação:</strong>
 * <ol>
 *   <li>o lote não existe, ou existe mas não pertence a {@code periodId} -- {@code 404} com a
 *       mesma mensagem nos dois casos, para não revelar a um chamador que um {@code batchId}
 *       copiado pertence a outro período (T-120-15 do threat model do plano);</li>
 *   <li>o lote é uma simulação ({@code dryRun}) -- não criou nada, não há inverso;</li>
 *   <li>a finalidade do lote é {@link FormGenerationBatch#READ_ONLY} -- não cria formulários;</li>
 *   <li>o lote já foi desfeito ({@code revertedAt != null}) -- {@link FormGenerationBatch#markReverted}
 *       também recusaria, mas recusar aqui evita chamar {@link FormGenerationBatchRevertService#revert}
 *       para nada.</li>
 * </ol>
 *
 * <p><strong>Sem anotação de transação a envolver o método.</strong> Mesma razão escrita no javadoc
 * de {@link FormGenerationBatchRevertService} e provada contra base real na Fase 117
 * ({@code PaaSubmissionPeriodExpiryScheduler}): uma transação única a envolver o varrimento
 * faria a falha de uma remoção arrastar as restantes -- o oposto da decisão 2 do operador
 * (2026-08-28). Cada remoção confirma na sua própria transação (o adaptador do plano 120-02), e
 * {@link FormGenerationBatchRepository#markReverted} confirma a sua. A consequência honesta: se o
 * processo morrer a meio do varrimento, ficam avaliações apagadas com o lote por marcar -- a
 * segunda passagem resolve, porque {@link FormGenerationBatchRevertService} regista
 * {@link FormGenerationRevertSkipReason#EVALUATION_NOT_FOUND} em vez de rebentar quando a
 * avaliação já não existe. É o desenho, não um descuido.
 */
@Component
public class RevertFormGenerationBatchCommandHandler
        implements CommandHandler<RevertFormGenerationBatchCommand, ResponseEntity<FormGenerationRevertResultDTO>> {

    private static final String SYSTEM_FALLBACK = "system-bot@nosi.cv";

    private final FormGenerationBatchRepository batchRepository;
    private final FormGenerationBatchRevertService revertService;

    public RevertFormGenerationBatchCommandHandler(FormGenerationBatchRepository batchRepository,
                                                     FormGenerationBatchRevertService revertService) {
        this.batchRepository = batchRepository;
        this.revertService = revertService;
    }

    @IgrpCommandHandler
    @Override
    public ResponseEntity<FormGenerationRevertResultDTO> handle(RevertFormGenerationBatchCommand command) {
        FormGenerationBatch batch = batchRepository.findById(command.getBatchId())
                .filter(found -> found.getPeriodId().equals(command.getPeriodId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Lote de geração não encontrado"));

        if (batch.isDryRun()) {
            throw IgrpResponseStatusException.conflict(
                    "Esta geração é uma simulação: não criou formulários, não há nada a desfazer.");
        }
        if (FormGenerationBatch.READ_ONLY.equals(batch.getGenerationMode())) {
            throw IgrpResponseStatusException.conflict(
                    "Esta finalidade não cria formulários; não há nada a desfazer.");
        }
        if (batch.getRevertedAt() != null) {
            throw IgrpResponseStatusException.conflict(
                    "Esta geração já foi desfeita em " + batch.getRevertedAt() + ".");
        }

        FormGenerationBatchRevertService.RevertOutcome outcome = revertService.revert(batch);

        String revertedBy = resolveAuthor();
        LocalDateTime revertedAt = LocalDateTime.now(AppTimeZone.CABO_VERDE);

        FormGenerationBatch marked = batchRepository.markReverted(batch.getId(), outcome.revertedFormIds(),
                outcome.blockedFormIds(), revertedAt, revertedBy);

        return ResponseEntity.ok(toResultDTO(marked, batch, outcome));
    }

    /**
     * O autor vem de quem invocou: o nome do {@link Authentication} corrente, e só na ausência
     * dele o âmbito de autor de sistema de {@link SystemAuditor#current()} -- uma reversão feita
     * por pessoa assina essa pessoa (o ponto da Fase 117). {@link #SYSTEM_FALLBACK} é o mesmo
     * literal genérico de {@code ApplicationAuditorAware}, usado só se nenhum dos dois primeiros
     * resolver.
     */
    private String resolveAuthor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String name = authentication.getName();
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return SystemAuditor.current().orElse(SYSTEM_FALLBACK);
    }

    /**
     * Monta o resultado a partir do lote já marcado (contagens, estado, {@code revertedAt}/
     * {@code revertedBy} persistidos) e do {@code outcome} do varrimento (para saber, item a
     * item, quem ficou bloqueado e porquê -- os metadados de cada item vêm de {@code original},
     * o lote antes da marcação, porque {@code markReverted} não devolve necessariamente as
     * mesmas instâncias de {@link FormGenerationBatchItem}).
     */
    private FormGenerationRevertResultDTO toResultDTO(FormGenerationBatch marked, FormGenerationBatch original,
                                                        FormGenerationBatchRevertService.RevertOutcome outcome) {
        FormGenerationRevertResultDTO dto = new FormGenerationRevertResultDTO();
        dto.setPeriodId(marked.getPeriodId().toString());
        dto.setBatchId(marked.getId().toString());
        dto.setStatus(marked.getStatus() != null ? marked.getStatus().getCode() : null);
        dto.setRevertedCount(outcome.revertedFormIds().size());
        dto.setBlockedCount(outcome.blockedFormIds().size());
        dto.setRevertedAt(marked.getRevertedAt());
        dto.setRevertedBy(marked.getRevertedBy());

        Map<UUID, FormGenerationBatchItem> itemsByGeneratedFormId = new HashMap<>();
        for (FormGenerationBatchItem item : original.getItems()) {
            if (item.getGeneratedFormId() != null) {
                itemsByGeneratedFormId.put(item.getGeneratedFormId(), item);
            }
        }

        for (Map.Entry<UUID, FormGenerationRevertSkipReason> entry : outcome.blockedFormIds().entrySet()) {
            dto.getBlocked().add(toSkippedItemDTO(entry.getKey(), entry.getValue(),
                    itemsByGeneratedFormId.get(entry.getKey())));
        }

        return dto;
    }

    private FormGenerationRevertSkippedItemDTO toSkippedItemDTO(UUID generatedFormId,
                                                                  FormGenerationRevertSkipReason reason,
                                                                  FormGenerationBatchItem item) {
        FormGenerationRevertSkippedItemDTO dto = new FormGenerationRevertSkippedItemDTO();
        dto.setGeneratedFormId(generatedFormId.toString());
        dto.setReason(reason.getCode());
        dto.setReasonDescription(reason.getDescription());
        if (item != null) {
            dto.setEmployeeId(item.getEmployeeId() != null ? item.getEmployeeId().toString() : null);
            dto.setEmployeeName(item.getEmployeeName());
            dto.setUnitId(item.getUnitId() != null ? item.getUnitId().toString() : null);
            dto.setUnitName(item.getUnitName());
        }
        return dto;
    }
}
