package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ItemChecklistId {

    private final ExternalID valor;

    private ItemChecklistId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ItemChecklistId não pode ser nulo");
        this.valor = valor;
    }

    public static ItemChecklistId from(UUID uuid) { return new ItemChecklistId(ExternalID.from(uuid)); }
    public static ItemChecklistId from(String uuidString) { return new ItemChecklistId(ExternalID.from(uuidString)); }
    public static ItemChecklistId gerarNovo() { return new ItemChecklistId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemChecklistId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
