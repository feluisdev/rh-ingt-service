package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class DocumentTypeId {

    private final ExternalID valor;

    private DocumentTypeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("DocumentTypeId não pode ser nulo");
        this.valor = valor;
    }

    public static DocumentTypeId from(ExternalID externalID) { return new DocumentTypeId(externalID); }
    public static DocumentTypeId from(UUID uuid) { return new DocumentTypeId(ExternalID.from(uuid)); }
    public static DocumentTypeId from(String uuidString) { return new DocumentTypeId(ExternalID.from(uuidString)); }
    public static DocumentTypeId gerarNovo() { return new DocumentTypeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentTypeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
