package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class ReciboVencimentoId {

    private final ExternalID valor;

    private ReciboVencimentoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ReciboVencimentoId não pode ser nulo");
        this.valor = valor;
    }

    public static ReciboVencimentoId gerarNovo() { return new ReciboVencimentoId(ExternalID.gerarNovo()); }
    public static ReciboVencimentoId from(UUID uuid) { return new ReciboVencimentoId(ExternalID.from(uuid)); }
    public static ReciboVencimentoId from(String str) { return new ReciboVencimentoId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReciboVencimentoId)) return false;
        return Objects.equals(valor, ((ReciboVencimentoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
