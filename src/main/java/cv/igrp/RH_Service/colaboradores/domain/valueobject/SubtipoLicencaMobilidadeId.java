package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class SubtipoLicencaMobilidadeId {

    private final ExternalID valor;

    private SubtipoLicencaMobilidadeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("SubtipoLicencaMobilidadeId não pode ser nulo");
        this.valor = valor;
    }

    public static SubtipoLicencaMobilidadeId gerarNovo() {
        return new SubtipoLicencaMobilidadeId(ExternalID.gerarNovo());
    }

    public static SubtipoLicencaMobilidadeId from(UUID uuid) {
        return new SubtipoLicencaMobilidadeId(ExternalID.from(uuid));
    }

    public static SubtipoLicencaMobilidadeId from(String str) {
        return new SubtipoLicencaMobilidadeId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubtipoLicencaMobilidadeId)) return false;
        return Objects.equals(valor, ((SubtipoLicencaMobilidadeId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
