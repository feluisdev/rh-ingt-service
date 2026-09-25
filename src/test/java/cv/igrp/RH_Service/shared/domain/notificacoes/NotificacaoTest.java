package cv.igrp.RH_Service.shared.domain.notificacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** BR-NOT-01..03: um destino, titulo obrigatorio, texto cortado, leitura idempotente. */
class NotificacaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 25, 10, 0);
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    @Test
    void vaiParaUmaPessoaOuParaUmPerfilNuncaAsDuas() {
        assertThrows(IllegalArgumentException.class,
                () -> Notificacao.criar(pessoa, PerfilDestino.RH, TipoNotificacao.AVISO, "t", null, null, null, AGORA));
        assertThrows(IllegalArgumentException.class,
                () -> Notificacao.criar(null, null, TipoNotificacao.AVISO, "t", null, null, null, AGORA));
        var n = Notificacao.criar(null, PerfilDestino.RH, null, "Para o RH", null, null, null, AGORA);
        assertEquals(PerfilDestino.RH, n.getPerfil());
        assertEquals(TipoNotificacao.AVISO, n.getTipo());
        assertFalse(n.eDe(pessoa));
    }

    @Test
    void semTituloNaoHaNotificacao() {
        assertThrows(IllegalArgumentException.class,
                () -> Notificacao.criar(pessoa, null, TipoNotificacao.AVISO, "  ", "texto", null, null, AGORA));
    }

    @Test
    void textoLongoCortaSeEmVezDeFalhar() {
        var n = Notificacao.criar(pessoa, null, TipoNotificacao.AVISO, "t", "x".repeat(3000), null, null, AGORA);
        assertEquals(Notificacao.MAX_TEXTO, n.getTexto().length());
        assertTrue(n.getTexto().endsWith("…"));
    }

    @Test
    void textoEmBrancoFicaNulo() {
        var n = Notificacao.criar(pessoa, null, TipoNotificacao.AVISO, "t", "   ", null, null, AGORA);
        assertNull(n.getTexto());
    }

    @Test
    void marcarLidaGuardaAPrimeiraData() {
        var n = Notificacao.criar(pessoa, null, TipoNotificacao.AVISO, "t", null, "X", "1", AGORA);
        assertFalse(n.isLida());
        n.marcarLida(AGORA.plusHours(1));
        n.marcarLida(AGORA.plusHours(5));
        assertEquals(AGORA.plusHours(1), n.getLidaEm());
        assertTrue(n.eDe(pessoa));
    }
}
