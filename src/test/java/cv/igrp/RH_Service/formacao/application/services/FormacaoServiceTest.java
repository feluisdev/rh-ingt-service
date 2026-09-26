package cv.igrp.RH_Service.formacao.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.services.ChefiaService;
import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacaoTest;
import cv.igrp.RH_Service.formacao.domain.repository.FormacaoRepositorio;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-FRM: em /me, o próprio pede e a chefia inscreve e decide só a sua equipa; concluir cria o histórico. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormacaoServiceTest {

    @Mock private FormacaoRepositorio repository;
    @Mock private FormacaoRepository historico;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ChefiaService chefiaService;
    @Mock private NotificacaoRepository notificacaoRepository;
    private FormacaoService service;
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final FuncionarioId membro = FuncionarioId.gerarNovo();
    private final FuncionarioId outro = FuncionarioId.gerarNovo();
    private AccaoFormacao accao;

    @BeforeEach
    void setUp() {
        service = new FormacaoService(repository, historico, funcionarioRepository, chefiaService, new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return AccaoFormacaoTest.INICIO.minusDays(10); }
        };
        var f = mock(Funcionario.class);
        when(f.getIsActive()).thenReturn(true);
        when(f.getNomeCompleto()).thenReturn("Ana");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(chefiaService.eChefeDirecto(chefe, membro)).thenReturn(true);
        when(chefiaService.chefeDirecto(membro)).thenReturn(Optional.of(chefe));
        accao = AccaoFormacaoTest.accao(10, false, null);
        accao.abrirInscricoes();
        when(repository.findById(accao.getId())).thenReturn(Optional.of(accao));
        when(repository.save(any(AccaoFormacao.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void chefiaInscreveEDecideSoASuaEquipa() {
        service.inscreverComo(chefe, accao.getId(), membro);
        assertEquals(AccaoFormacao.EstadoInscricao.ADMITIDA, accao.inscricaoDe(membro).orElseThrow().getEstado());
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.inscreverComo(chefe, accao.getId(), outro));
        assertEquals(403, ex.getStatusCode().value());
        service.inscreverComo(outro, accao.getId(), null);
        var pedido = accao.inscricaoDe(outro).orElseThrow();
        assertEquals(AccaoFormacao.EstadoInscricao.PEDIDA, pedido.getEstado());
        assertThrows(IgrpResponseStatusException.class, () -> service.decidirComo(chefe, accao.getId(), pedido.getId(), true, null));
    }

    @Test
    void concluirPoeOsAproveitamentosNoHistorico() {
        service.inscrever(accao.getId(), membro);
        service.inscrever(accao.getId(), outro);
        service.iniciar(accao.getId());
        service.avaliar(accao.getId(), accao.inscricaoDe(membro).orElseThrow().getId(), AccaoFormacao.EstadoInscricao.APROVEITAMENTO, 5);
        service.avaliar(accao.getId(), accao.inscricaoDe(outro).orElseThrow().getId(), AccaoFormacao.EstadoInscricao.SEM_APROVEITAMENTO, 3);
        service.concluir(accao.getId());
        verify(historico, times(1)).save(any(Formacao.class));
    }
}
