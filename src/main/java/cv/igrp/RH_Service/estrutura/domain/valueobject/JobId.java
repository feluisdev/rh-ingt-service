package cv.igrp.RH_Service.estrutura.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class JobId {

    private final ExternalID valor;

    private JobId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("JobId não pode ser nulo");
        this.valor = valor;
    }

    public static JobId from(ExternalID externalID) { return new JobId(externalID); }
    public static JobId from(UUID uuid) { return new JobId(ExternalID.from(uuid)); }
    public static JobId from(String uuidString) { return new JobId(ExternalID.from(uuidString)); }
    public static JobId gerarNovo() { return new JobId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
