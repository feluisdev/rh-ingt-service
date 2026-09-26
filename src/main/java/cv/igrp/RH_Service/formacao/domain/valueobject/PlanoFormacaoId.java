package cv.igrp.RH_Service.formacao.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PlanoFormacaoId {

    private final ExternalID valor;

    private PlanoFormacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PlanoFormacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static PlanoFormacaoId from(UUID uuid) { return new PlanoFormacaoId(ExternalID.from(uuid)); }
    public static PlanoFormacaoId from(String uuidString) { return new PlanoFormacaoId(ExternalID.from(uuidString)); }
    public static PlanoFormacaoId gerarNovo() { return new PlanoFormacaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanoFormacaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
