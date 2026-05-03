package cv.igrp.RH_Service.parametrizacoes.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class WorkerStateId {

    private final ExternalID valor;

    private WorkerStateId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("WorkerStateId não pode ser nulo");
        this.valor = valor;
    }

    public static WorkerStateId from(ExternalID externalID) { return new WorkerStateId(externalID); }
    public static WorkerStateId from(UUID uuid) { return new WorkerStateId(ExternalID.from(uuid)); }
    public static WorkerStateId from(String uuidString) { return new WorkerStateId(ExternalID.from(uuidString)); }
    public static WorkerStateId gerarNovo() { return new WorkerStateId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerStateId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
