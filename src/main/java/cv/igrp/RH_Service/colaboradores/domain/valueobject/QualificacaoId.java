package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class QualificacaoId {

    private final ExternalID valor;

    private QualificacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("QualificacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static QualificacaoId gerarNovo() {
        return new QualificacaoId(ExternalID.gerarNovo());
    }

    public static QualificacaoId from(UUID uuid) {
        return new QualificacaoId(ExternalID.from(uuid));
    }

    public static QualificacaoId from(String str) {
        return new QualificacaoId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QualificacaoId)) return false;
        return Objects.equals(valor, ((QualificacaoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
