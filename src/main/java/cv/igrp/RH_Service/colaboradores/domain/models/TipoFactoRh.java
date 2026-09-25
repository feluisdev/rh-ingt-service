package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * O que aconteceu a um colaborador que o <b>processamento salarial</b> precisa de saber (BR-FAC-01). O
 * RH produz os factos; a outra aplicação calcula remunerações e descontos. Guarda-se pelo nome
 * ({@code t_facto_rh.tipo}) — não se renomeiam valores já usados.
 */
public enum TipoFactoRh {
    /** Entrada ao serviço: primeira colocação num Lugar. */
    ADMISSAO,
    /** Volta a ter Lugar depois de ter ficado sem ele. */
    REINGRESSO,
    /** Mudança de escalão na mesma categoria. */
    PROGRESSAO,
    /** Mudança de categoria (sobe na carreira). */
    PROMOCAO,
    /** Mudança de Lugar mantendo carreira, categoria e escalão. */
    TRANSFERENCIA,
    /** Mudança de carreira. */
    MUDANCA_CARREIRA,
    /** A mobilidade consolidou-se num Lugar do destino. */
    CONSOLIDACAO_MOBILIDADE,
    /** Mudança de estado/situação funcional que não cessa o vínculo (ex.: inactividade no quadro). */
    MUDANCA_SITUACAO,
    /** Fim da relação de emprego público. */
    CESSACAO,
    /** Provimento: nomeação, contrato ou estágio (entrada ao serviço). */
    PROVIMENTO,
    /** Início ou fim de comissão de serviço. */
    COMISSAO_SERVICO,
    /** Suspensão preventiva em processo disciplinar (com ou sem perda do vencimento de exercício). */
    SUSPENSAO_PREVENTIVA,
    /** Pena disciplinar com efeito na remuneração ou na antiguidade. */
    PENA_DISCIPLINAR,
    /** Missão de serviço realizada: os dias de ajudas de custo (sem valores). */
    MISSAO_SERVICO,
    /** Acidente em serviço: incapacidade temporária (mantém a remuneração, art. 189.º n.º 2). */
    ACIDENTE_SERVICO,
    /** Pré-aposentação: suspensão do vínculo com prestação mensal (art. 179.º). */
    PRE_APOSENTACAO,
    /** Aposentação (a cessação é o facto CESSACAO; este marca a modalidade). */
    APOSENTACAO
}
