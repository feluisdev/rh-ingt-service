package cv.igrp.RH_Service.estrutura.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class OrganizationalUnitId {

    private final ExternalID valor;

    private OrganizationalUnitId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("OrganizationalUnitId não pode ser nulo");
        this.valor = valor;
    }

    public static OrganizationalUnitId from(ExternalID externalID) { return new OrganizationalUnitId(externalID); }
    public static OrganizationalUnitId from(UUID uuid) { return new OrganizationalUnitId(ExternalID.from(uuid)); }
    public static OrganizationalUnitId from(String uuidString) { return new OrganizationalUnitId(ExternalID.from(uuidString)); }
    public static OrganizationalUnitId gerarNovo() { return new OrganizationalUnitId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationalUnitId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
