package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class DependenteId {

    private final ExternalID valor;

    private DependenteId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("DependenteId não pode ser nulo");
        this.valor = valor;
    }

    public static DependenteId gerarNovo() {
        return new DependenteId(ExternalID.gerarNovo());
    }

    public static DependenteId from(UUID uuid) {
        return new DependenteId(ExternalID.from(uuid));
    }

    public static DependenteId from(String str) {
        return new DependenteId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DependenteId)) return false;
        return Objects.equals(valor, ((DependenteId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
