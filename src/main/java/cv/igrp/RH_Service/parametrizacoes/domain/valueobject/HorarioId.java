package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class HorarioId {

    private final ExternalID valor;

    private HorarioId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("HorarioId não pode ser nulo");
        this.valor = valor;
    }

    public static HorarioId from(UUID uuid) { return new HorarioId(ExternalID.from(uuid)); }
    public static HorarioId from(String uuidString) { return new HorarioId(ExternalID.from(uuidString)); }
    public static HorarioId gerarNovo() { return new HorarioId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
