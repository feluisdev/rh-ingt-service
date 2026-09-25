package cv.igrp.RH_Service.shared.application.services.notificacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** BR-NOT-06: cada um so le e marca as suas; a caixa partilhada marca-se para o perfil. */
@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    private static final LocalDateTime ONTEM = LocalDateTime.of(2026, 9, 24, 9, 0);
    @Mock private NotificacaoRepository repository;
    @InjectMocks private NotificacaoService service;
    private final FuncionarioId eu = FuncionarioId.gerarNovo();
    private final FuncionarioId outro = FuncionarioId.gerarNovo();

    @Test
    void marcoLidaUmaMinha() {
        var n = Notificacao.criar(eu, null, TipoNotificacao.AVISO, "t", null, null, null, ONTEM);
        when(repository.findById(n.getId())).thenReturn(Optional.of(n));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertTrue(service.marcarLida(eu, n.getId()).isLida());
    }

    @Test
    void aDeOutraPessoaNaoExisteParaMim() {
        var n = Notificacao.criar(outro, null, TipoNotificacao.AVISO, "t", null, null, null, ONTEM);
        when(repository.findById(n.getId())).thenReturn(Optional.of(n));
        var e = assertThrows(IgrpResponseStatusException.class, () -> service.marcarLida(eu, n.getId()));
        assertEquals(404, e.getStatusCode().value());
    }

    @Test
    void aDaCaixaDoRhNaoSeMarcaComoPessoal() {
        var n = Notificacao.criar(null, PerfilDestino.RH, TipoNotificacao.AVISO, "t", null, null, null, ONTEM);
        when(repository.findById(n.getId())).thenReturn(Optional.of(n));
        assertThrows(IgrpResponseStatusException.class, () -> service.marcarLida(eu, n.getId()));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertTrue(service.marcarLida(PerfilDestino.RH, n.getId()).isLida());
    }

    @Test
    void tamanhoDaPaginaTemTecto() {
        service.minhas(eu, false, -3, 5000);
        verify(repository).findDoDestinatario(eq(eu), anyBoolean(), eq(0), eq(NotificacaoService.TAMANHO_MAXIMO));
        service.doPerfil(PerfilDestino.RH, true, 1, 0);
        verify(repository).findDoPerfil(eq(PerfilDestino.RH), eq(true), eq(1), anyInt());
    }
}
