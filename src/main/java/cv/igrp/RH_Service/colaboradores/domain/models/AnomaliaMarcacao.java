package cv.igrp.RH_Service.colaboradores.domain.models;

/** O que impede de emparelhar as marcações de um dia. Um dia com anomalias tem de ser corrigido. */
public enum AnomaliaMarcacao {
    /** A última entrada do dia não tem saída. */
    ENTRADA_SEM_SAIDA,
    /** Uma saída sem entrada aberta antes dela. */
    SAIDA_SEM_ENTRADA,
    /** Duas entradas seguidas: conta a segunda. */
    ENTRADAS_SEGUIDAS
}
