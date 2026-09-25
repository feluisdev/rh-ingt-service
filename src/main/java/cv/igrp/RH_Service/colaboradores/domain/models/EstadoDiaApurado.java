package cv.igrp.RH_Service.colaboradores.domain.models;

/** O que um dia do mês é, para o apuramento de faltas. Só {@link #COM_FALTA} tem tempo em falta. */
public enum EstadoDiaApurado {
    /** Hoje ou depois: o dia ainda não acabou. */
    FUTURO,
    /** Antes da admissão. */
    FORA_DO_VINCULO,
    /** Isenção de horário no contrato: sem horário obrigatório, sem débito. */
    ISENTO,
    FERIADO,
    /** Coberto por um pedido de ausência aprovado (férias, falta justificada...). */
    AUSENCIA_JUSTIFICADA,
    /** Em licença. */
    LICENCA,
    /** Em mobilidade externa: trabalha noutra entidade. */
    MOBILIDADE_EXTERNA,
    /** Em missão de serviço (deslocado por motivo de serviço, Lei n.º 20/X/2023, art. 159.º): conta como serviço. */
    MISSAO_SERVICO,
    /** Em acção de formação com inscrição aprovada: conta como serviço. */
    FORMACAO,
    /** Suspenso do serviço: preventivamente ou a cumprir pena de suspensão ou inactividade (Estatuto Disciplinar). */
    SUSPENSAO_DISCIPLINAR,
    /** Incapacidade temporária por acidente em serviço ou doença profissional: falta justificada sem perda de direitos (art. 188.º). */
    ACIDENTE_SERVICO,
    /** Sem horário que valha nesse dia (nem próprio, nem da unidade, nem base). */
    SEM_HORARIO,
    /** O horário não tem blocos nesse dia da semana. */
    DESCANSO,
    /** Há pedidos de correcção por decidir nesse dia: não se apura até a chefia ou o RH os decidirem. */
    POR_VALIDAR,
    /** As marcações têm anomalias: não se apura sem as corrigir. */
    POR_CORRIGIR,
    SEM_FALTA,
    COM_FALTA
}
