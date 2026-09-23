package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * De onde veio uma marcação. Várias formas de registar convivem sobre o mesmo modelo, e cada
 * marcação diz a sua: o relógio é a fonte principal, o RH lança as excepções e as correcções.
 */
public enum OrigemMarcacao {
    /** Lançada pelo RH — uma excepção ou uma correcção. */
    MANUAL,
    /** Vinda de um relógio de ponto, pela importação. */
    IMPORTADO,
    /** Registada pelo próprio (/me) — passo seguinte, com validação da chefia. */
    PROPRIO
}
