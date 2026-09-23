package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * O ciclo de vida de um trabalho suplementar. Lançado pela chefia ou pelo RH nasce AUTORIZADO; pedido
 * pelo próprio nasce PEDIDO e espera a decisão da chefia directa ou do RH.
 */
public enum EstadoTrabalhoSuplementar {
    PEDIDO,
    AUTORIZADO,
    RECUSADO,
    CANCELADO
}
