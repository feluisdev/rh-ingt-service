package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ParametroFeriasId {

    private final ExternalID valor;

    private ParametroFeriasId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ParametroFeriasId não pode ser nulo");
        this.valor = valor;
    }

    public static ParametroFeriasId from(UUID uuid) { return new ParametroFeriasId(ExternalID.from(uuid)); }
    public static ParametroFeriasId from(String uuidString) { return new ParametroFeriasId(ExternalID.from(uuidString)); }
    public static ParametroFeriasId gerarNovo() { return new ParametroFeriasId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParametroFeriasId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
