package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class DocumentoEmitidoId {

    private final ExternalID valor;

    private DocumentoEmitidoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("DocumentoEmitidoId não pode ser nulo");
        this.valor = valor;
    }

    public static DocumentoEmitidoId from(UUID uuid) { return new DocumentoEmitidoId(ExternalID.from(uuid)); }
    public static DocumentoEmitidoId from(String uuidString) { return new DocumentoEmitidoId(ExternalID.from(uuidString)); }
    public static DocumentoEmitidoId gerarNovo() { return new DocumentoEmitidoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentoEmitidoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
