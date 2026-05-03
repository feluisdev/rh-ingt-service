package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;
import java.util.UUID;

public final class ColocacaoId {

    private final ExternalID valor;

    private ColocacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ColocacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static ColocacaoId gerarNovo() { return new ColocacaoId(ExternalID.gerarNovo()); }
    public static ColocacaoId from(UUID uuid) { return new ColocacaoId(ExternalID.from(uuid)); }
    public static ColocacaoId from(String str) { return new ColocacaoId(ExternalID.from(str)); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColocacaoId)) return false;
        return Objects.equals(valor, ((ColocacaoId) o).valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
