package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PublicHolidayId {

    private final ExternalID valor;

    private PublicHolidayId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PublicHolidayId não pode ser nulo");
        this.valor = valor;
    }

    public static PublicHolidayId from(ExternalID externalID) { return new PublicHolidayId(externalID); }
    public static PublicHolidayId from(UUID uuid) { return new PublicHolidayId(ExternalID.from(uuid)); }
    public static PublicHolidayId from(String uuidString) { return new PublicHolidayId(ExternalID.from(uuidString)); }
    public static PublicHolidayId gerarNovo() { return new PublicHolidayId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicHolidayId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
