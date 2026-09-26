package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ExoneracaoId {

    private final ExternalID valor;

    private ExoneracaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ExoneracaoId não pode ser nulo");
        this.valor = valor;
    }

    public static ExoneracaoId from(UUID uuid) { return new ExoneracaoId(ExternalID.from(uuid)); }
    public static ExoneracaoId from(String uuidString) { return new ExoneracaoId(ExternalID.from(uuidString)); }
    public static ExoneracaoId gerarNovo() { return new ExoneracaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExoneracaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
