package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ProcessoAposentacaoId {

    private final ExternalID valor;

    private ProcessoAposentacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ProcessoAposentacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static ProcessoAposentacaoId from(UUID uuid) { return new ProcessoAposentacaoId(ExternalID.from(uuid)); }
    public static ProcessoAposentacaoId from(String uuidString) { return new ProcessoAposentacaoId(ExternalID.from(uuidString)); }
    public static ProcessoAposentacaoId gerarNovo() { return new ProcessoAposentacaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessoAposentacaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
