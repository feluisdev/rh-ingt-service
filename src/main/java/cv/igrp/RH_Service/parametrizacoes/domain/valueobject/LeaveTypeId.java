package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class LeaveTypeId {

    private final ExternalID valor;

    private LeaveTypeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("LeaveTypeId não pode ser nulo");
        this.valor = valor;
    }

    public static LeaveTypeId from(ExternalID externalID) { return new LeaveTypeId(externalID); }
    public static LeaveTypeId from(UUID uuid) { return new LeaveTypeId(ExternalID.from(uuid)); }
    public static LeaveTypeId from(String uuidString) { return new LeaveTypeId(ExternalID.from(uuidString)); }
    public static LeaveTypeId gerarNovo() { return new LeaveTypeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveTypeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
