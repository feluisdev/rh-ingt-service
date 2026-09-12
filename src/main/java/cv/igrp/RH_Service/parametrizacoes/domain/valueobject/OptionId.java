package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class OptionId {

    private final ExternalID valor;

    private OptionId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("OptionId não pode ser nulo");
        this.valor = valor;
    }

    public static OptionId from(ExternalID externalID) { return new OptionId(externalID); }
    public static OptionId from(UUID uuid) { return new OptionId(ExternalID.from(uuid)); }
    public static OptionId from(String uuidString) { return new OptionId(ExternalID.from(uuidString)); }
    public static OptionId gerarNovo() { return new OptionId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptionId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
