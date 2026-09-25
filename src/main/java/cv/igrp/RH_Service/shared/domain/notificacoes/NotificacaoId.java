package cv.igrp.RH_Service.shared.domain.notificacoes;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.UUID;

public final class NotificacaoId {

    private final ExternalID valor;

    private NotificacaoId(ExternalID valor) {
        if (valor == null) throw new IllegalArgumentException("NotificacaoId não pode ser nulo");
        this.valor = valor;
    }

    public static NotificacaoId from(UUID uuid) { return new NotificacaoId(ExternalID.from(uuid)); }
    public static NotificacaoId from(String uuidString) { return new NotificacaoId(ExternalID.from(uuidString)); }
    public static NotificacaoId gerarNovo() { return new NotificacaoId(ExternalID.gerarNovo()); }

    public UUID getValor() { return valor.getValor(); }
    public String getStringValor() { return valor.getStringValor(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificacaoId that)) return false;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }
}
