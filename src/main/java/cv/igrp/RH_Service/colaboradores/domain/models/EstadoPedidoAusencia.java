package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Estado do pedido de ausência. Eram quatro cadeias soltas comparadas com
 * {@code equals} pelos handlers, incluindo um {@code "PENDENTE"} escrito à mão.
 *
 * <p>Não é parametrizável: é o percurso de um pedido com aprovação. O que varia
 * entre instituições é <b>que</b> ausências precisam de aprovação
 * ({@code t_leave_type.requires_approval}) e quantos dias dão direito.
 */
public enum EstadoPedidoAusencia {

    /** Submetido, à espera de decisão. Os dias já estão reservados no saldo. */
    PENDENTE,

    /** Deferido: os dias passam de reservados a gozados. */
    APROVADO,

    /** Indeferido: a reserva é libertada. */
    REJEITADO,

    /** Retirado pelo próprio ou pelo RH; devolve ao saldo o que tinha tirado. */
    CANCELADO;

    public boolean isPendente() { return this == PENDENTE; }
    public boolean isAprovado() { return this == APROVADO; }

    /** Já não conta para os limites anuais: foi indeferido ou retirado. */
    public boolean isSemEfeito() { return this == REJEITADO || this == CANCELADO; }

    public static EstadoPedidoAusencia de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return EstadoPedidoAusencia.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Estado de pedido inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }

    public static String texto(EstadoPedidoAusencia estado) {
        return estado == null ? null : estado.name();
    }
}
