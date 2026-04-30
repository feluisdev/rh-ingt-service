package cv.igrp.RH_Service.carreiras.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class GradeId {

    private final ExternalID valor;

    private GradeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("GradeId não pode ser nulo");
        this.valor = valor;
    }

    public static GradeId gerarNovo() {
        return new GradeId(ExternalID.gerarNovo());
    }

    public static GradeId from(UUID uuid) {
        return new GradeId(ExternalID.from(uuid));
    }

    public static GradeId from(String str) {
        return new GradeId(ExternalID.from(str));
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
        if (!(o instanceof GradeId)) return false;
        GradeId other = (GradeId) o;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }
}
