package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class MissaoServicoId {

    private final ExternalID valor;

    private MissaoServicoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("MissaoServicoId não pode ser nulo");
        this.valor = valor;
    }

    public static MissaoServicoId from(UUID uuid) { return new MissaoServicoId(ExternalID.from(uuid)); }
    public static MissaoServicoId from(String uuidString) { return new MissaoServicoId(ExternalID.from(uuidString)); }
    public static MissaoServicoId gerarNovo() { return new MissaoServicoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MissaoServicoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
