package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class LeaveMobilitySubtypeId {

    private final ExternalID valor;

    private LeaveMobilitySubtypeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("LeaveMobilitySubtypeId não pode ser nulo");
        this.valor = valor;
    }

    public static LeaveMobilitySubtypeId from(ExternalID externalID) { return new LeaveMobilitySubtypeId(externalID); }
    public static LeaveMobilitySubtypeId from(UUID uuid) { return new LeaveMobilitySubtypeId(ExternalID.from(uuid)); }
    public static LeaveMobilitySubtypeId from(String uuidString) { return new LeaveMobilitySubtypeId(ExternalID.from(uuidString)); }
    public static LeaveMobilitySubtypeId gerarNovo() { return new LeaveMobilitySubtypeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveMobilitySubtypeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
