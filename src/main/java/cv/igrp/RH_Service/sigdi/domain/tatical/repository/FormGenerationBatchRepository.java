package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de domínio para o rasto de lotes de geração de formulários (Fase 119,
 * {@code PRZ-06}/{@code PRZ-07}). Consumida pelos planos 03 (escreve), 04 e 05 (lêem), e pela
 * Fase 120 ({@code PRZ-04}, reversão de lotes).
 */
public interface FormGenerationBatchRepository {

    FormGenerationBatch save(FormGenerationBatch batch);

    Optional<FormGenerationBatch> findById(UUID id);

    /**
     * Devolve os lotes do período por {@code generatedAt} descendente -- o lote mais recente
     * primeiro. Contrato de ordem: o plano 05 depende dele para escolher o lote a mostrar.
     */
    List<FormGenerationBatch> findByPeriodId(UUID periodId);

    List<FormGenerationBatch> findByPeriodIds(Collection<UUID> periodIds);

    /**
     * Apaga o lote e, por cascata na base de dados, as suas linhas filhas. Existe para a
     * Fase 120 (reversão de lotes) e não é chamado por esta fase -- não é código morto.
     */
    void deleteById(UUID id);
}
