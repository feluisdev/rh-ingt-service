package cv.igrp.RH_Service.colaboradores.domain.models;

/** Porque é que um dia tem tempo em falta (DL n.º 3/2010, art. 13.º n.º 1). */
public enum MotivoFalta {
    /** Nenhuma marcação no dia: conta o dia inteiro. O RH confirma que não falta importar o relógio. */
    SEM_REGISTO,
    /** Horário fixo: blocos do horário que nenhum período de presença cobre (atraso, saída antecipada...). */
    INCOMPLETO,
    /** Horário flexível: plataformas fixas não cobertas. */
    PLATAFORMA
}
