package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * Onde se presta o trabalho — Lei n.º 20/X/2023, art. 166.º. Os três valores são da lei, por isso
 * enum. Mudam o que é falta (art. 170.º): ausência do local no período normal (presencial e misto)
 * ou indisponibilidade no horário estipulado (teletrabalho).
 */
public enum RegimePrestacao {
    PRESENCIAL,
    TELETRABALHO,
    MISTO
}
