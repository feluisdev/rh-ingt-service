package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class SaldoAusenciaId {

    private final ExternalID valor;

    private SaldoAusenciaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("SaldoAusenciaId não pode ser nulo");
        this.valor = valor;
    }

    public static SaldoAusenciaId gerarNovo() {
        return new SaldoAusenciaId(ExternalID.gerarNovo());
    }

    public static SaldoAusenciaId from(UUID uuid) {
        return new SaldoAusenciaId(ExternalID.from(uuid));
    }

    public static SaldoAusenciaId from(String str) {
        return new SaldoAusenciaId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaldoAusenciaId)) return false;
        return Objects.equals(valor, ((SaldoAusenciaId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
