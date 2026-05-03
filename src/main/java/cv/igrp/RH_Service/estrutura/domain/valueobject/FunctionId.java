package cv.igrp.RH_Service.estrutura.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class FunctionId {

    private final ExternalID valor;

    private FunctionId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FunctionId não pode ser nulo");
        this.valor = valor;
    }

    public static FunctionId from(ExternalID externalID) { return new FunctionId(externalID); }
    public static FunctionId from(UUID uuid) { return new FunctionId(ExternalID.from(uuid)); }
    public static FunctionId from(String uuidString) { return new FunctionId(ExternalID.from(uuidString)); }
    public static FunctionId gerarNovo() { return new FunctionId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
