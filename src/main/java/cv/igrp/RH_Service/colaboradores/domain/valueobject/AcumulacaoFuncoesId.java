package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class AcumulacaoFuncoesId {

    private final ExternalID valor;

    private AcumulacaoFuncoesId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("AcumulacaoFuncoesId não pode ser nulo");
        this.valor = valor;
    }

    public static AcumulacaoFuncoesId from(UUID uuid) { return new AcumulacaoFuncoesId(ExternalID.from(uuid)); }
    public static AcumulacaoFuncoesId from(String uuidString) { return new AcumulacaoFuncoesId(ExternalID.from(uuidString)); }
    public static AcumulacaoFuncoesId gerarNovo() { return new AcumulacaoFuncoesId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcumulacaoFuncoesId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
