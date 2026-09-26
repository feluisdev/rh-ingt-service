package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ChecklistId {

    private final ExternalID valor;

    private ChecklistId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ChecklistId não pode ser nulo");
        this.valor = valor;
    }

    public static ChecklistId from(UUID uuid) { return new ChecklistId(ExternalID.from(uuid)); }
    public static ChecklistId from(String uuidString) { return new ChecklistId(ExternalID.from(uuidString)); }
    public static ChecklistId gerarNovo() { return new ChecklistId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChecklistId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
