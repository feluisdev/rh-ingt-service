package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PublicacaoOficialId {

    private final ExternalID valor;

    private PublicacaoOficialId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PublicacaoOficialId não pode ser nulo");
        this.valor = valor;
    }

    public static PublicacaoOficialId from(UUID uuid) { return new PublicacaoOficialId(ExternalID.from(uuid)); }
    public static PublicacaoOficialId from(String uuidString) { return new PublicacaoOficialId(ExternalID.from(uuidString)); }
    public static PublicacaoOficialId gerarNovo() { return new PublicacaoOficialId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicacaoOficialId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
