package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class AssignmentId {

    private final ExternalID valor;

    private AssignmentId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("AssignmentId não pode ser nulo");
        this.valor = valor;
    }

    public static AssignmentId gerarNovo() { return new AssignmentId(ExternalID.gerarNovo()); }
    public static AssignmentId from(UUID uuid) { return new AssignmentId(ExternalID.from(uuid)); }
    public static AssignmentId from(String str) { return new AssignmentId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentId)) return false;
        return Objects.equals(valor, ((AssignmentId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
