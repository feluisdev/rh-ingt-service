package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class FeriasDoAnoId {

    private final ExternalID valor;

    private FeriasDoAnoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FeriasDoAnoId não pode ser nulo");
        this.valor = valor;
    }

    public static FeriasDoAnoId gerarNovo() {
        return new FeriasDoAnoId(ExternalID.gerarNovo());
    }

    public static FeriasDoAnoId from(UUID uuid) {
        return new FeriasDoAnoId(ExternalID.from(uuid));
    }

    public static FeriasDoAnoId from(String str) {
        return new FeriasDoAnoId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeriasDoAnoId)) return false;
        return Objects.equals(valor, ((FeriasDoAnoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
