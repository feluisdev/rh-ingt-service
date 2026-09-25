package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PeriodoProvaId {

    private final ExternalID valor;

    private PeriodoProvaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PeriodoProvaId não pode ser nulo");
        this.valor = valor;
    }

    public static PeriodoProvaId from(UUID uuid) { return new PeriodoProvaId(ExternalID.from(uuid)); }
    public static PeriodoProvaId from(String uuidString) { return new PeriodoProvaId(ExternalID.from(uuidString)); }
    public static PeriodoProvaId gerarNovo() { return new PeriodoProvaId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeriodoProvaId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
