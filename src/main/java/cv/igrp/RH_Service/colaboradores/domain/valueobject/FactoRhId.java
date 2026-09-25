package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class FactoRhId {

    private final ExternalID valor;

    private FactoRhId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FactoRhId não pode ser nulo");
        this.valor = valor;
    }

    public static FactoRhId from(UUID uuid) { return new FactoRhId(ExternalID.from(uuid)); }
    public static FactoRhId from(String uuidString) { return new FactoRhId(ExternalID.from(uuidString)); }
    public static FactoRhId gerarNovo() { return new FactoRhId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactoRhId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
