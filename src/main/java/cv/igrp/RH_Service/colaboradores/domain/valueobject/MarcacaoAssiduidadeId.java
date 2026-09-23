package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class MarcacaoAssiduidadeId {

    private final ExternalID valor;

    private MarcacaoAssiduidadeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("MarcacaoAssiduidadeId não pode ser nulo");
        this.valor = valor;
    }

    public static MarcacaoAssiduidadeId from(UUID uuid) { return new MarcacaoAssiduidadeId(ExternalID.from(uuid)); }
    public static MarcacaoAssiduidadeId from(String uuidString) { return new MarcacaoAssiduidadeId(ExternalID.from(uuidString)); }
    public static MarcacaoAssiduidadeId gerarNovo() { return new MarcacaoAssiduidadeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarcacaoAssiduidadeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
