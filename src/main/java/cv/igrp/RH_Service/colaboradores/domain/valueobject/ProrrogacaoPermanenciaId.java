package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ProrrogacaoPermanenciaId {

    private final ExternalID valor;

    private ProrrogacaoPermanenciaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ProrrogacaoPermanenciaId não pode ser nulo");
        this.valor = valor;
    }

    public static ProrrogacaoPermanenciaId from(UUID uuid) { return new ProrrogacaoPermanenciaId(ExternalID.from(uuid)); }
    public static ProrrogacaoPermanenciaId from(String uuidString) { return new ProrrogacaoPermanenciaId(ExternalID.from(uuidString)); }
    public static ProrrogacaoPermanenciaId gerarNovo() { return new ProrrogacaoPermanenciaId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProrrogacaoPermanenciaId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
