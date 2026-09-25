package cv.igrp.RH_Service.colaboradores.domain.models;

/** O ciclo de um processo de aposentação: PEDIDO → DEFERIDO → DESLIGADO → CONCLUIDO; INDEFERIDO; CANCELADO. */
public enum EstadoProcessoAposentacao {
    PEDIDO,
    DEFERIDO,
    INDEFERIDO,
    /** Desligado do serviço aguardando a aposentação (art. 120.º n.º 1 d)) — ou em pré-aposentação. */
    DESLIGADO,
    /** Aposentado: o vínculo cessou. */
    CONCLUIDO,
    CANCELADO;

    public boolean emCurso() {
        return this == PEDIDO || this == DEFERIDO || this == DESLIGADO;
    }
}
