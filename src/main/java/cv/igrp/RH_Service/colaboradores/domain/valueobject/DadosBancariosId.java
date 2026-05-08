package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class DadosBancariosId {

    private final ExternalID valor;

    private DadosBancariosId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("DadosBancariosId não pode ser nulo");
        this.valor = valor;
    }

    public static DadosBancariosId gerarNovo() {
        return new DadosBancariosId(ExternalID.gerarNovo());
    }

    public static DadosBancariosId from(UUID uuid) {
        return new DadosBancariosId(ExternalID.from(uuid));
    }

    public static DadosBancariosId from(String str) {
        return new DadosBancariosId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DadosBancariosId)) return false;
        return Objects.equals(valor, ((DadosBancariosId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
