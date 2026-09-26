package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class JuntaMedicaId {

    private final ExternalID valor;

    private JuntaMedicaId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("JuntaMedicaId não pode ser nulo");
        this.valor = valor;
    }

    public static JuntaMedicaId from(UUID uuid) { return new JuntaMedicaId(ExternalID.from(uuid)); }
    public static JuntaMedicaId from(String uuidString) { return new JuntaMedicaId(ExternalID.from(uuidString)); }
    public static JuntaMedicaId gerarNovo() { return new JuntaMedicaId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JuntaMedicaId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
