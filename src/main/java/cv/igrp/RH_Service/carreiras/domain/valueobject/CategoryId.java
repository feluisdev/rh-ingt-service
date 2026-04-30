package cv.igrp.RH_Service.carreiras.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class CategoryId {

    private final ExternalID valor;

    private CategoryId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("CategoryId não pode ser nulo");
        this.valor = valor;
    }

    public static CategoryId gerarNovo() {
        return new CategoryId(ExternalID.gerarNovo());
    }

    public static CategoryId from(UUID uuid) {
        return new CategoryId(ExternalID.from(uuid));
    }

    public static CategoryId from(String str) {
        return new CategoryId(ExternalID.from(str));
    }

    public UUID getValor() {
        return valor.getValor();
    }

    public String getStringValor() {
        return valor.getStringValor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryId)) return false;
        CategoryId other = (CategoryId) o;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}
