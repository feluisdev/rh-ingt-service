package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class VinculoLaboralId {

    private final ExternalID valor;

    private VinculoLaboralId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("VinculoLaboralId não pode ser nulo");
        this.valor = valor;
    }

    public static VinculoLaboralId from(ExternalID externalID) { return new VinculoLaboralId(externalID); }
    public static VinculoLaboralId from(UUID uuid) { return new VinculoLaboralId(ExternalID.from(uuid)); }
    public static VinculoLaboralId from(String uuidString) { return new VinculoLaboralId(ExternalID.from(uuidString)); }
    public static VinculoLaboralId gerarNovo() { return new VinculoLaboralId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VinculoLaboralId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
