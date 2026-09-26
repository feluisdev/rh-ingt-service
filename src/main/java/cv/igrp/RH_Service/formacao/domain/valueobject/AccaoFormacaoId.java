package cv.igrp.RH_Service.formacao.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class AccaoFormacaoId {

    private final ExternalID valor;

    private AccaoFormacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("AccaoFormacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static AccaoFormacaoId from(UUID uuid) { return new AccaoFormacaoId(ExternalID.from(uuid)); }
    public static AccaoFormacaoId from(String uuidString) { return new AccaoFormacaoId(ExternalID.from(uuidString)); }
    public static AccaoFormacaoId gerarNovo() { return new AccaoFormacaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccaoFormacaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
