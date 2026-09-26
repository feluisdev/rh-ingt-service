package cv.igrp.RH_Service.formacao.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class NecessidadeFormacaoId {

    private final ExternalID valor;

    private NecessidadeFormacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("NecessidadeFormacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static NecessidadeFormacaoId from(UUID uuid) { return new NecessidadeFormacaoId(ExternalID.from(uuid)); }
    public static NecessidadeFormacaoId from(String uuidString) { return new NecessidadeFormacaoId(ExternalID.from(uuidString)); }
    public static NecessidadeFormacaoId gerarNovo() { return new NecessidadeFormacaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NecessidadeFormacaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
