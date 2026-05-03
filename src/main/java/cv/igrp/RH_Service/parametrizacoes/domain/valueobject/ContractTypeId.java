package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ContractTypeId {

    private final ExternalID valor;

    private ContractTypeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ContractTypeId não pode ser nulo");
        this.valor = valor;
    }

    public static ContractTypeId from(ExternalID externalID) { return new ContractTypeId(externalID); }
    public static ContractTypeId from(UUID uuid) { return new ContractTypeId(ExternalID.from(uuid)); }
    public static ContractTypeId from(String uuidString) { return new ContractTypeId(ExternalID.from(uuidString)); }
    public static ContractTypeId gerarNovo() { return new ContractTypeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractTypeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
