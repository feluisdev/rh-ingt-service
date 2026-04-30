package cv.igrp.RH_Service.carreiras.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class CareerId {

    private final ExternalID valor;

    private CareerId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("CareerId não pode ser nulo");
        this.valor = valor;
    }

    public static CareerId gerarNovo() {
        return new CareerId(ExternalID.gerarNovo());
    }

    public static CareerId from(UUID uuid) {
        return new CareerId(ExternalID.from(uuid));
    }

    public static CareerId from(String str) {
        return new CareerId(ExternalID.from(str));
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
        if (!(o instanceof CareerId)) return false;
        CareerId other = (CareerId) o;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}
