package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class PedidoDeclaracaoId {

    private final ExternalID valor;

    private PedidoDeclaracaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("PedidoDeclaracaoId não pode ser nulo");
        this.valor = valor;
    }

    public static PedidoDeclaracaoId from(UUID uuid) { return new PedidoDeclaracaoId(ExternalID.from(uuid)); }
    public static PedidoDeclaracaoId from(String uuidString) { return new PedidoDeclaracaoId(ExternalID.from(uuidString)); }
    public static PedidoDeclaracaoId gerarNovo() { return new PedidoDeclaracaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoDeclaracaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
