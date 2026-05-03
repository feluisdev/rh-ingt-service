package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class DocumentoId {

    private final ExternalID valor;

    private DocumentoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("DocumentoId não pode ser nulo");
        this.valor = valor;
    }

    public static DocumentoId gerarNovo() { return new DocumentoId(ExternalID.gerarNovo()); }
    public static DocumentoId from(UUID uuid) { return new DocumentoId(ExternalID.from(uuid)); }
    public static DocumentoId from(String str) { return new DocumentoId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentoId)) return false;
        return Objects.equals(valor, ((DocumentoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
