package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class ContratoId {

    private final ExternalID valor;

    private ContratoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ContratoId não pode ser nulo");
        this.valor = valor;
    }

    public static ContratoId gerarNovo() {
        return new ContratoId(ExternalID.gerarNovo());
    }

    public static ContratoId from(UUID uuid) {
        return new ContratoId(ExternalID.from(uuid));
    }

    public static ContratoId from(String str) {
        return new ContratoId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContratoId)) return false;
        return Objects.equals(valor, ((ContratoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
