package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class ProcessoDisciplinarId {

    private final ExternalID valor;

    private ProcessoDisciplinarId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ProcessoDisciplinarId não pode ser nulo");
        this.valor = valor;
    }

    public static ProcessoDisciplinarId gerarNovo() { return new ProcessoDisciplinarId(ExternalID.gerarNovo()); }
    public static ProcessoDisciplinarId from(UUID uuid) { return new ProcessoDisciplinarId(ExternalID.from(uuid)); }
    public static ProcessoDisciplinarId from(String str) { return new ProcessoDisciplinarId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessoDisciplinarId)) return false;
        return Objects.equals(valor, ((ProcessoDisciplinarId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
