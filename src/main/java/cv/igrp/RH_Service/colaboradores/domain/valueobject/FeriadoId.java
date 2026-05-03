package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class FeriadoId {

    private final ExternalID valor;

    private FeriadoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FeriadoId não pode ser nulo");
        this.valor = valor;
    }

    public static FeriadoId gerarNovo() {
        return new FeriadoId(ExternalID.gerarNovo());
    }

    public static FeriadoId from(UUID uuid) {
        return new FeriadoId(ExternalID.from(uuid));
    }

    public static FeriadoId from(String str) {
        return new FeriadoId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeriadoId)) return false;
        return Objects.equals(valor, ((FeriadoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
