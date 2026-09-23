package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * Onde cai o trabalho suplementar — as situações que a Lei n.º 20/X/2023, art. 155.º n.º 2 a),
 * distingue: para além do horário num dia de trabalho, em dia de descanso ou em feriado. Fixado pela
 * lei, por isso enum. Calcula-se do horário vigente e do calendário de feriados, não se guarda.
 */
public enum TipoDiaSuplementar {
    DIA_UTIL,
    DESCANSO,
    FERIADO
}
