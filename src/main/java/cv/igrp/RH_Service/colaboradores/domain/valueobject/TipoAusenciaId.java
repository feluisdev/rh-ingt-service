package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class TipoAusenciaId {

    private final ExternalID valor;

    private TipoAusenciaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("TipoAusenciaId não pode ser nulo");
        this.valor = valor;
    }

    public static TipoAusenciaId gerarNovo() {
        return new TipoAusenciaId(ExternalID.gerarNovo());
    }

    public static TipoAusenciaId from(UUID uuid) {
        return new TipoAusenciaId(ExternalID.from(uuid));
    }

    public static TipoAusenciaId from(String str) {
        return new TipoAusenciaId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoAusenciaId)) return false;
        return Objects.equals(valor, ((TipoAusenciaId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
