package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class FormacaoId {

    private final ExternalID valor;

    private FormacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FormacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static FormacaoId gerarNovo() { return new FormacaoId(ExternalID.gerarNovo()); }
    public static FormacaoId from(UUID uuid) { return new FormacaoId(ExternalID.from(uuid)); }
    public static FormacaoId from(String str) { return new FormacaoId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormacaoId)) return false;
        return Objects.equals(valor, ((FormacaoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
