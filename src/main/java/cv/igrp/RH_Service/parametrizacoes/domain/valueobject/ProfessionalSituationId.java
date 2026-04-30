package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ProfessionalSituationId {

    private final ExternalID valor;

    private ProfessionalSituationId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ProfessionalSituationId não pode ser nulo");
        this.valor = valor;
    }

    public static ProfessionalSituationId from(ExternalID externalID) { return new ProfessionalSituationId(externalID); }
    public static ProfessionalSituationId from(UUID uuid) { return new ProfessionalSituationId(ExternalID.from(uuid)); }
    public static ProfessionalSituationId from(String uuidString) { return new ProfessionalSituationId(ExternalID.from(uuidString)); }
    public static ProfessionalSituationId gerarNovo() { return new ProfessionalSituationId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfessionalSituationId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
