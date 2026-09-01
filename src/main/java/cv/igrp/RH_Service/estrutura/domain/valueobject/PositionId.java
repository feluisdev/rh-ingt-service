package cv.igrp.RH_Service.estrutura.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PositionId {

    private final ExternalID valor;

    private PositionId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PositionId não pode ser nulo");
        this.valor = valor;
    }

    public static PositionId from(ExternalID externalID) { return new PositionId(externalID); }
    public static PositionId from(UUID uuid) { return new PositionId(ExternalID.from(uuid)); }
    public static PositionId from(String uuidString) { return new PositionId(ExternalID.from(uuidString)); }
    public static PositionId gerarNovo() { return new PositionId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
