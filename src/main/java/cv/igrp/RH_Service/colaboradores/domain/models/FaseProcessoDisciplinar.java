package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * As fases do processo disciplinar. Um processo antigo, só registado (sem tramitação), não tem fase.
 * PARTICIPADO → INSTAURADO → EM_INSTRUCAO → ACUSADO → RELATORIO → DECIDIDO → NOTIFICADO (→ EM_RECURSO) → CONCLUIDO;
 * ARQUIVADO antes da decisão, ou pela decisão.
 */
public enum FaseProcessoDisciplinar {
    PARTICIPADO,
    INSTAURADO,
    EM_INSTRUCAO,
    ACUSADO,
    RELATORIO,
    DECIDIDO,
    NOTIFICADO,
    EM_RECURSO,
    CONCLUIDO,
    ARQUIVADO;

    public boolean emCurso() {
        return this != CONCLUIDO && this != ARQUIVADO;
    }

    /** Antes da decisão: o arguido é arguido (Lei n.º 20/X/2023, art. 95.º a)). */
    public boolean antesDaDecisao() {
        return ordinal() < DECIDIDO.ordinal();
    }
}
