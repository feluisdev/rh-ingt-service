package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ReservaLugarId {

    private final ExternalID valor;

    private ReservaLugarId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ReservaLugarId não pode ser nulo");
        this.valor = valor;
    }

    public static ReservaLugarId from(UUID uuid) { return new ReservaLugarId(ExternalID.from(uuid)); }
    public static ReservaLugarId from(String uuidString) { return new ReservaLugarId(ExternalID.from(uuidString)); }
    public static ReservaLugarId gerarNovo() { return new ReservaLugarId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaLugarId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
