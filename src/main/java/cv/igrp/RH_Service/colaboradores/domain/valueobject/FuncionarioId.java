package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class FuncionarioId {

    private final ExternalID valor;

    private FuncionarioId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("FuncionarioId não pode ser nulo");
        this.valor = valor;
    }

    public static FuncionarioId gerarNovo() {
        return new FuncionarioId(ExternalID.gerarNovo());
    }

    public static FuncionarioId from(UUID uuid) {
        return new FuncionarioId(ExternalID.from(uuid));
    }

    public static FuncionarioId from(String str) {
        return new FuncionarioId(ExternalID.from(str));
    }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FuncionarioId)) return false;
        return Objects.equals(valor, ((FuncionarioId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
