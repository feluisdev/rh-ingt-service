package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.models.ChecklistTest;
import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.ChecklistRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-CHK: abre pelos factos sem duplicar; o próprio e a chefia só marcam os seus; avisos dos prazos. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChecklistServiceTest {

    @Mock private ChecklistRepository repository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private ChefiaService chefiaService;
    @Mock private NotificacaoRepository notificacaoRepository;
    private ChecklistService service;
    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final FuncionarioId chefe = FuncionarioId.gerarNovo();
    private final LocalDate hoje = ChecklistTest.ENTRADA;

    @BeforeEach
    void setUp() {
        service = new ChecklistService(repository, funcionarioRepository, chefiaService, new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return hoje; }
        };
        var f = mock(Funcionario.class);
        when(f.getNomeCompleto()).thenReturn("Ana");
        when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
        when(repository.save(any(Checklist.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.findModelos(any())).thenAnswer(i -> ChecklistTest.modelo().stream()
                .filter(m -> m.getTipo() == i.getArgument(0)).toList());
        when(repository.findCorrente(any(), any())).thenReturn(Optional.empty());
        when(chefiaService.chefeDirecto(pessoa)).thenReturn(Optional.of(chefe));
        when(chefiaService.eChefeDirecto(chefe, pessoa)).thenReturn(true);
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void facto(TipoFactoRh tipo) {
        var f = mock(FactoRh.class);
        when(f.getTipo()).thenReturn(tipo);
        when(f.getFuncionarioId()).thenReturn(pessoa);
        when(f.getDataEfeito()).thenReturn(hoje);
        service.aoRegistarFacto(new DiarioFactos.FactoRegistado(f));
    }

    @Test
    void admissaoAbreEntradaECessacaoSaida() {
        facto(TipoFactoRh.ADMISSAO);
        facto(TipoFactoRh.CESSACAO);
        facto(TipoFactoRh.PROGRESSAO);
        verify(repository, times(2)).save(any(Checklist.class));
        // RH duas vezes; a chefia (acolhimento) na de entrada.
        verify(notificacaoRepository, times(3)).save(any(Notificacao.class));
    }

    @Test
    void naoDuplicaAAberta() {
        var aberta = Checklist.abrir(pessoa, TipoChecklist.ENTRADA, hoje, ChecklistTest.modelo(), hoje);
        when(repository.findCorrente(pessoa, TipoChecklist.ENTRADA)).thenReturn(Optional.of(aberta));
        facto(TipoFactoRh.REINGRESSO);
        verify(repository, never()).save(any(Checklist.class));
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.abrir(pessoa, TipoChecklist.ENTRADA, hoje));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void soAChefiaDirectaMarcaOsItensDaChefia() {
        var c = Checklist.abrir(pessoa, TipoChecklist.ENTRADA, hoje, ChecklistTest.modelo(), hoje);
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        var acolhimento = c.getItens().get(0).getId();
        var cartao = c.getItens().get(1).getId();
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.marcarComo(pessoa, c.getId(), acolhimento, Checklist.EstadoItem.FEITO, null, null));
        assertEquals(403, ex.getStatusCode().value());
        assertThrows(IgrpResponseStatusException.class,
                () -> service.marcarComo(chefe, c.getId(), cartao, Checklist.EstadoItem.FEITO, null, null));
        service.marcarComo(chefe, c.getId(), acolhimento, Checklist.EstadoItem.FEITO, null, null);
        assertEquals(Checklist.EstadoItem.FEITO, c.getItens().get(0).getEstado());
    }

    @Test
    void cumprirSemChecklistNaoFazNada() {
        assertTrue(service.cumprir(pessoa, TipoChecklist.ENTRADA, ChecklistService.CARTAO_PROFISSIONAL, null, null).isEmpty());
        var c = Checklist.abrir(pessoa, TipoChecklist.ENTRADA, hoje, ChecklistTest.modelo(), hoje);
        when(repository.findCorrente(pessoa, TipoChecklist.ENTRADA)).thenReturn(Optional.of(c));
        assertTrue(service.cumprir(pessoa, TipoChecklist.ENTRADA, ChecklistService.CARTAO_PROFISSIONAL, "n.º 1", null).isPresent());
    }

    @Test
    void avisaOsPrazosDeOntem() {
        var c = Checklist.abrir(pessoa, TipoChecklist.ENTRADA, hoje, ChecklistTest.modelo(), hoje);
        var diaDoAviso = hoje.plusDays(31);
        when(repository.findComPrazoEm(hoje.plusDays(30))).thenReturn(List.of(c));
        assertEquals(1, service.avisarPrazos(diaDoAviso));
        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }
}
