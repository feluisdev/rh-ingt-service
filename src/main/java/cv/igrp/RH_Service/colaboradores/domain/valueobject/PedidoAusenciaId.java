package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class PedidoAusenciaId {

    private final ExternalID valor;

    private PedidoAusenciaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PedidoAusenciaId não pode ser nulo");
        this.valor = valor;
    }

    public static PedidoAusenciaId gerarNovo() {
        return new PedidoAusenciaId(ExternalID.gerarNovo());
    }

    public static PedidoAusenciaId from(UUID uuid) {
        return new PedidoAusenciaId(ExternalID.from(uuid));
    }

    public static PedidoAusenciaId from(String str) {
        return new PedidoAusenciaId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoAusenciaId)) return false;
        return Objects.equals(valor, ((PedidoAusenciaId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
