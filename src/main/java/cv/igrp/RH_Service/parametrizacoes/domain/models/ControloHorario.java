package cv.igrp.RH_Service.parametrizacoes.domain.models;

/**
 * Como se controla o cumprimento de um horário — a única diferença entre modalidades que as
 * regras usam. A modalidade (rígido, desfasado, jornada contínua...) é o nome que a instituição
 * dá ao horário: o diploma de desenvolvimento que as define (Lei n.º 20/X/2023, art. 165.º
 * n.º 2) não está publicado, e não se adivinha.
 */
public enum ControloHorario {
    /** Horas marcadas: o período normal são os blocos. */
    FIXO,
    /**
     * Plataformas fixas e margens: cumpre-se uma duração diária, e a falta é o débito apurado
     * no fim de cada período de aferição (DL n.º 3/2010, art. 13.º n.º 2).
     */
    FLEXIVEL
}
