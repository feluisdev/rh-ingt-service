package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class MapaFeriasId {

    private final ExternalID valor;

    private MapaFeriasId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("MapaFeriasId não pode ser nulo");
        this.valor = valor;
    }

    public static MapaFeriasId gerarNovo() {
        return new MapaFeriasId(ExternalID.gerarNovo());
    }

    public static MapaFeriasId from(UUID uuid) {
        return new MapaFeriasId(ExternalID.from(uuid));
    }

    public static MapaFeriasId from(String str) {
        return new MapaFeriasId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapaFeriasId)) return false;
        return Objects.equals(valor, ((MapaFeriasId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
