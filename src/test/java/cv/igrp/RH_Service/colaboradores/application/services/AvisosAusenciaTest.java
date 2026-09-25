package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-NOT-04: pendente avisa a chefia directa (ou o RH sem chefia); decidido avisa quem pediu. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvisosAusenciaTest {

    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private ChefiaService chefiaService;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    private AvisosAusencia avisos;

    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();

    @BeforeEach
    void setUp() {
        avisos = new AvisosAusencia(new Notificador(notificacaoRepository), chefiaService, funcionarioRepository, tipoAusenciaRepository);
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());
        when(tipoAusenciaRepository.findById(any())).thenReturn(Optional.empty());
    }

    private PedidoAusencia pendente() {
        return PedidoAusencia.criar(pessoa, TipoAusenciaId.gerarNovo(), LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 11, 14), 10, "Ferias", null);
    }

    private Notificacao gravada() {
        var c = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(c.capture());
        return c.getValue();
    }

    @Test
    void pendenteVaiParaAChefiaDirecta() {
        when(chefiaService.chefeDirecto(pessoa)).thenReturn(Optional.of(chefe));
        avisos.pedidoCriado(pendente());
        var n = gravada();
        assertEquals(chefe, n.getDestinatario());
        assertTrue(n.getTitulo().contains("de 03/11/2026 a 14/11/2026"), n.getTitulo());
    }

    @Test
    void semChefiaVaiParaACaixaDoRh() {
        when(chefiaService.chefeDirecto(pessoa)).thenReturn(Optional.empty());
        avisos.pedidoCriado(pendente());
        assertEquals(PerfilDestino.RH, gravada().getPerfil());
    }

    @Test
    void aprovadoAutomaticamenteNaoAvisaNinguem() {
        var p = pendente();
        p.aprovarAutomaticamente(LocalDate.of(2026, 9, 25));
        avisos.pedidoCriado(p);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void decididoAvisaQuemPediu() {
        var p = pendente();
        p.aprovar(chefe, LocalDate.of(2026, 9, 25), "Bom descanso");
        avisos.pedidoDecidido(p);
        var n = gravada();
        assertEquals(pessoa, n.getDestinatario());
        assertTrue(n.getTitulo().endsWith("foi aprovado"), n.getTitulo());
        assertEquals("Bom descanso", n.getTexto());
    }
}
