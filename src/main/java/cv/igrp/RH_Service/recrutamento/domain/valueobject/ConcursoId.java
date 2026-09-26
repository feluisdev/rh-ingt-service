package cv.igrp.RH_Service.recrutamento.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ConcursoId {

    private final ExternalID valor;

    private ConcursoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ConcursoId não pode ser nulo");
        this.valor = valor;
    }

    public static ConcursoId from(UUID uuid) { return new ConcursoId(ExternalID.from(uuid)); }
    public static ConcursoId from(String uuidString) { return new ConcursoId(ExternalID.from(uuidString)); }
    public static ConcursoId gerarNovo() { return new ConcursoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcursoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
