package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class HistoricoEstadoColaboradorId {

    private final ExternalID valor;

    private HistoricoEstadoColaboradorId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("HistoricoEstadoColaboradorId não pode ser nulo");
        this.valor = valor;
    }

    public static HistoricoEstadoColaboradorId gerarNovo() {
        return new HistoricoEstadoColaboradorId(ExternalID.gerarNovo());
    }

    public static HistoricoEstadoColaboradorId from(UUID uuid) {
        return new HistoricoEstadoColaboradorId(ExternalID.from(uuid));
    }

    public static HistoricoEstadoColaboradorId from(String str) {
        return new HistoricoEstadoColaboradorId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricoEstadoColaboradorId)) return false;
        return Objects.equals(valor, ((HistoricoEstadoColaboradorId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
