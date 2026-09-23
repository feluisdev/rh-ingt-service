package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class TrabalhoSuplementarId {

    private final ExternalID valor;

    private TrabalhoSuplementarId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("TrabalhoSuplementarId não pode ser nulo");
        this.valor = valor;
    }

    public static TrabalhoSuplementarId from(UUID uuid) { return new TrabalhoSuplementarId(ExternalID.from(uuid)); }
    public static TrabalhoSuplementarId from(String uuidString) { return new TrabalhoSuplementarId(ExternalID.from(uuidString)); }
    public static TrabalhoSuplementarId gerarNovo() { return new TrabalhoSuplementarId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrabalhoSuplementarId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
