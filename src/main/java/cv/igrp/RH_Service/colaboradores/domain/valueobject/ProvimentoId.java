package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ProvimentoId {

    private final ExternalID valor;

    private ProvimentoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ProvimentoId não pode ser nulo");
        this.valor = valor;
    }

    public static ProvimentoId from(UUID uuid) { return new ProvimentoId(ExternalID.from(uuid)); }
    public static ProvimentoId from(String uuidString) { return new ProvimentoId(ExternalID.from(uuidString)); }
    public static ProvimentoId gerarNovo() { return new ProvimentoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvimentoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
