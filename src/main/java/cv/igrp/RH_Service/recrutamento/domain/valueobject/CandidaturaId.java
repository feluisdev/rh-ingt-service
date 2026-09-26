package cv.igrp.RH_Service.recrutamento.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class CandidaturaId {

    private final ExternalID valor;

    private CandidaturaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("CandidaturaId não pode ser nulo");
        this.valor = valor;
    }

    public static CandidaturaId from(UUID uuid) { return new CandidaturaId(ExternalID.from(uuid)); }
    public static CandidaturaId from(String uuidString) { return new CandidaturaId(ExternalID.from(uuidString)); }
    public static CandidaturaId gerarNovo() { return new CandidaturaId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidaturaId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
