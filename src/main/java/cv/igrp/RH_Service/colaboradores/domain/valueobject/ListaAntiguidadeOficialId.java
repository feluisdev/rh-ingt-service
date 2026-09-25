package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ListaAntiguidadeOficialId {

    private final ExternalID valor;

    private ListaAntiguidadeOficialId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ListaAntiguidadeOficialId não pode ser nulo");
        this.valor = valor;
    }

    public static ListaAntiguidadeOficialId from(UUID uuid) { return new ListaAntiguidadeOficialId(ExternalID.from(uuid)); }
    public static ListaAntiguidadeOficialId from(String uuidString) { return new ListaAntiguidadeOficialId(ExternalID.from(uuidString)); }
    public static ListaAntiguidadeOficialId gerarNovo() { return new ListaAntiguidadeOficialId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListaAntiguidadeOficialId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
