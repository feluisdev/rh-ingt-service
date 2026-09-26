package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class AcidenteServicoId {

    private final ExternalID valor;

    private AcidenteServicoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("AcidenteServicoId não pode ser nulo");
        this.valor = valor;
    }

    public static AcidenteServicoId from(UUID uuid) { return new AcidenteServicoId(ExternalID.from(uuid)); }
    public static AcidenteServicoId from(String uuidString) { return new AcidenteServicoId(ExternalID.from(uuidString)); }
    public static AcidenteServicoId gerarNovo() { return new AcidenteServicoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcidenteServicoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
