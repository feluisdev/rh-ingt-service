package cv.igrp.RH_Service.shared.application.services.notificacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** BR-NOT-04..05: a API fluente, destino opcional, nunca faz falhar quem chama, fila de e-mail. */
@ExtendWith(MockitoExtension.class)
class NotificadorTest {

    @Mock private NotificacaoRepository repository;
    private Notificador notificador;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        notificador = new Notificador(repository);
    }

    @Test
    void gravaComTodosOsCampos() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var n = notificador.para(pessoa).tipo(TipoNotificacao.PEDIDO_AUSENCIA_DECIDIDO)
                .titulo("Aprovado").texto("De 03/11/2026 a 14/11/2026.").recurso("PEDIDO_AUSENCIA", 42).enviar();
        assertTrue(n.isPresent());
        var c = ArgumentCaptor.forClass(Notificacao.class);
        verify(repository).save(c.capture());
        assertEquals(pessoa, c.getValue().getDestinatario());
        assertEquals(TipoNotificacao.PEDIDO_AUSENCIA_DECIDIDO, c.getValue().getTipo());
        assertEquals("PEDIDO_AUSENCIA", c.getValue().getRecursoTipo());
        assertEquals("42", c.getValue().getRecursoId());
        verify(repository, never()).enfileirarEmail(any(), any());
    }

    @Test
    void paraOrhVaiParaACaixaPartilhada() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var n = notificador.paraRh().titulo("Pedido de declaracao").enviar();
        assertEquals(PerfilDestino.RH, n.orElseThrow().getPerfil());
    }

    @Test
    void destinoOpcionalVazioNaoFazNada() {
        var n = notificador.para(Optional.<FuncionarioId>empty()).titulo("Sem chefia").enviar();
        assertTrue(n.isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void notificacaoMalCompostaNaoFazFalharQuemChama() {
        var n = notificador.para(pessoa).enviar();   // sem titulo
        assertTrue(n.isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void porEmailEnfileiraOEnvio() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var n = notificador.para(pessoa).titulo("Acusacao").porEmail().enviar().orElseThrow();
        verify(repository).enfileirarEmail(any(), any());
        assertEquals(pessoa, n.getDestinatario());
    }
}
