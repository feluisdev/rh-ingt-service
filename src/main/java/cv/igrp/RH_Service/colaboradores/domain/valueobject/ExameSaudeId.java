package cv.igrp.RH_Service.colaboradores.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class ExameSaudeId {

    private final ExternalID valor;

    private ExameSaudeId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("ExameSaudeId não pode ser nulo");
        this.valor = valor;
    }

    public static ExameSaudeId from(UUID uuid) { return new ExameSaudeId(ExternalID.from(uuid)); }
    public static ExameSaudeId from(String uuidString) { return new ExameSaudeId(ExternalID.from(uuidString)); }
    public static ExameSaudeId gerarNovo() { return new ExameSaudeId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExameSaudeId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
