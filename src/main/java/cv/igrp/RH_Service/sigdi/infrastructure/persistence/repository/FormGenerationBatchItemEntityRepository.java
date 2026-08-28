package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FormGenerationBatchItemEntityRepository extends JpaRepository<FormGenerationBatchItemEntity, UUID> {

    List<FormGenerationBatchItemEntity> findByBatchId(UUID batchId);

    List<FormGenerationBatchItemEntity> findByBatchIdIn(Collection<UUID> batchIds);

    /**
     * Fase 120, plano 01 (PRZ-04). Update em bloco -- aceitável aqui e só aqui porque
     * {@code t_form_generation_batch_item} não é auditada pelo Envers (D-02 do 119-01): não há
     * revisão nenhuma para o JPQL saltar. Em {@code t_siadap_evaluations}, que é auditada, a
     * mesma técnica perderia a revisão em silêncio -- ver o plano {@code 120-02}.
     *
     * <p>O filtro por {@code batchId} não é redundante com o filtro por
     * {@code generatedFormId in :generatedFormIds}: é o que garante que a reversão de um lote
     * nunca marca a linha de outro lote que aponte para o mesmo formulário.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FormGenerationBatchItemEntity i SET i.revertedAt = :revertedAt " +
            "WHERE i.batchId = :batchId AND i.generatedFormId IN :generatedFormIds")
    void markItemsReverted(@Param("batchId") UUID batchId,
                            @Param("generatedFormIds") Collection<UUID> generatedFormIds,
                            @Param("revertedAt") LocalDateTime revertedAt);

    /**
     * Fase 120, plano 01 (PRZ-04). Mesma razão de {@link #markItemsReverted} para o update em
     * bloco ser aceitável. Um motivo distinto de bloqueio é um update distinto -- quem agrupa
     * por motivo é {@code FormGenerationBatchRepositoryImpl.markReverted}, não esta query.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FormGenerationBatchItemEntity i SET i.revertSkipReason = :reasonCode " +
            "WHERE i.batchId = :batchId AND i.generatedFormId IN :generatedFormIds")
    void markItemsRevertBlocked(@Param("batchId") UUID batchId,
                                 @Param("generatedFormIds") Collection<UUID> generatedFormIds,
                                 @Param("reasonCode") String reasonCode);
}
