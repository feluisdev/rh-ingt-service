package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class LicencaMobilidadeId {

    private final ExternalID valor;

    private LicencaMobilidadeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("LicencaMobilidadeId não pode ser nulo");
        this.valor = valor;
    }

    public static LicencaMobilidadeId gerarNovo() {
        return new LicencaMobilidadeId(ExternalID.gerarNovo());
    }

    public static LicencaMobilidadeId from(UUID uuid) {
        return new LicencaMobilidadeId(ExternalID.from(uuid));
    }

    public static LicencaMobilidadeId from(String str) {
        return new LicencaMobilidadeId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LicencaMobilidadeId)) return false;
        return Objects.equals(valor, ((LicencaMobilidadeId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
