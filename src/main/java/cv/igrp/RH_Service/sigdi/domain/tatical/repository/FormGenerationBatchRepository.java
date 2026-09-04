package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de domínio para o rasto de lotes de geração de formulários (Fase 119,
 * {@code PRZ-06}/{@code PRZ-07}). Consumida pelos planos 03 (escreve), 04 e 05 (lêem), e pela
 * Fase 120 ({@code PRZ-04}, reversão de lotes).
 */
public interface FormGenerationBatchRepository {

    FormGenerationBatch save(FormGenerationBatch batch);

    /**
     * <strong>Traz os itens do lote.</strong> É o único método de leitura que os carrega; quem
     * precisar da lista de itens -- e não apenas das contagens -- tem de passar por aqui.
     */
    Optional<FormGenerationBatch> findById(UUID id);

    /**
     * Devolve os lotes do período por {@code generatedAt} descendente -- o lote mais recente
     * primeiro. Contrato de ordem: o plano 05 depende dele para escolher o lote a mostrar.
     *
     * <p><strong>NÃO traz os itens</strong> -- cada lote vem com a lista vazia, de propósito. Um
     * período acumula dezenas de lotes (só as simulações de um deles passam de vinte) e apenas um
     * chega a ser mostrado; carregar os itens de todos seria N+1 para exibir um. Depois de
     * escolher o lote, recarregue-o por {@link #findById(UUID)}.
     *
     * <p>Isto está escrito porque a sua ausência custou um defeito: o detalhe de geração devolveu
     * durante três planos as contagens certas e as quatro listas de itens vazias, e nenhum teste o
     * apanhou porque todos simulavam este método a devolver lotes já com itens -- mais generosos
     * do que o adaptador que representavam. Medido contra o serviço real a 2026-08-28, corrigido
     * no plano 07 da Fase 120.
     */
    List<FormGenerationBatch> findByPeriodId(UUID periodId);

    /**
     * <strong>NÃO traz os itens</strong>, pela mesma razão de {@link #findByPeriodId(UUID)} e com
     * mais força: serve a leitura em bloco da coluna do ecrã (D-30), que só precisa de contagens.
     */
    List<FormGenerationBatch> findByPeriodIds(Collection<UUID> periodIds);

    /**
     * Apaga o lote e, por cascata na base de dados, as suas linhas filhas. Existe para a
     * Fase 120 (reversão de lotes) e não é chamado por esta fase -- não é código morto.
     */
    void deleteById(UUID id);

    /**
     * Marca a reversão de um lote -- Fase 120, plano 01 (PRZ-04). Existe porque
     * {@link #save(FormGenerationBatch)} <strong>não</strong> serve para actualizar um lote
     * existente: o item de domínio ({@code FormGenerationBatchItem}) não tem {@code id}, o
     * mapper gera um {@code UUID} novo por item ao converter para entidade, e regravar um lote
     * reconstruído por {@code save} inseriria os itens outra vez, em duplicado. Isto é um facto
     * medido do {@code 119-01}, não uma preferência de estilo.
     *
     * @param batchId o lote a marcar
     * @param revertedFormIds os {@code generatedFormId} das avaliações que foram apagadas
     * @param blockedFormIds os {@code generatedFormId} que não puderam ser apagados, com o
     *        motivo de cada bloqueio
     * @param revertedAt o instante da reversão
     * @param revertedBy quem a fez -- nunca nulo nem em branco (ver
     *        {@code FormGenerationBatch#markReverted})
     * @return o lote recarregado, com o estado {@code REVERTED} ou {@code PARTIALLY_REVERTED}
     *         já derivado e os itens actualizados
     */
    FormGenerationBatch markReverted(UUID batchId, Collection<UUID> revertedFormIds,
                                      Map<UUID, FormGenerationRevertSkipReason> blockedFormIds,
                                      LocalDateTime revertedAt, String revertedBy);
}
