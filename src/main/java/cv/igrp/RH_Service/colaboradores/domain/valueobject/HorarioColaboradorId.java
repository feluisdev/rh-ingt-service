package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class HorarioColaboradorId {

    private final ExternalID valor;

    private HorarioColaboradorId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("HorarioColaboradorId não pode ser nulo");
        this.valor = valor;
    }

    public static HorarioColaboradorId from(UUID uuid) { return new HorarioColaboradorId(ExternalID.from(uuid)); }
    public static HorarioColaboradorId from(String uuidString) { return new HorarioColaboradorId(ExternalID.from(uuidString)); }
    public static HorarioColaboradorId gerarNovo() { return new HorarioColaboradorId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioColaboradorId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
