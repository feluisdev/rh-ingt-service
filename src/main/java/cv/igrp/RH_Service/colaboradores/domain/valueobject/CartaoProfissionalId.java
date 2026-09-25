package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class CartaoProfissionalId {

    private final ExternalID valor;

    private CartaoProfissionalId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("CartaoProfissionalId não pode ser nulo");
        this.valor = valor;
    }

    public static CartaoProfissionalId from(UUID uuid) { return new CartaoProfissionalId(ExternalID.from(uuid)); }
    public static CartaoProfissionalId from(String uuidString) { return new CartaoProfissionalId(ExternalID.from(uuidString)); }
    public static CartaoProfissionalId gerarNovo() { return new CartaoProfissionalId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartaoProfissionalId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
