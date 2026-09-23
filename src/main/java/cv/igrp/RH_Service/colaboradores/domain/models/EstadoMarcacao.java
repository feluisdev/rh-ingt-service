package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * Se uma marcação conta. As do relógio, as do RH e as picagens em tempo real pelo próprio nascem
 * VALIDA; um pedido de correcção do próprio nasce PENDENTE e só conta depois de validado pela chefia
 * directa ou pelo RH.
 */
public enum EstadoMarcacao {
    VALIDA,
    PENDENTE,
    REJEITADA
}
