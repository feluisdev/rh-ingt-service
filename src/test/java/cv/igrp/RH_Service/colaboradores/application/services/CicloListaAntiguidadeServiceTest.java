package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ListaAntiguidadeOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ListaAntiguidadeOficialRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-LAN-06..09: aprovar congela a lista gerada, uma por servico e ano; afixar avisa cada pessoa. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CicloListaAntiguidadeServiceTest {

    @Mock private ListaAntiguidadeOficialRepository repository;
    @Mock private ListaAntiguidadeService listaAntiguidadeService;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private NotificacaoRepository notificacaoRepository;
    private CicloListaAntiguidadeService service;
    private final UUID unidade = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CicloListaAntiguidadeService(repository, listaAntiguidadeService, funcionarioRepository,
                new Notificador(notificacaoRepository)) {
            @Override LocalDate hoje() { return LocalDate.of(2026, 2, 1); }
        };
        when(repository.save(any(ListaAntiguidadeOficial.class))).thenAnswer(i -> i.getArgument(0));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var a = mock(Funcionario.class);
        when(a.getId()).thenReturn(FuncionarioId.gerarNovo());
        var b = mock(Funcionario.class);
        when(b.getId()).thenReturn(FuncionarioId.gerarNovo());
        var ant = new CalculadoraAntiguidade.Antiguidade(LocalDate.of(2015, 1, 1), LocalDate.of(2025, 12, 31), 4018, 0, 4018, 11, 0, 3, List.of());
        var grupo = new ListaAntiguidadeService.Grupo("Regime Geral", "Tecnico", false, List.of(
                new ListaAntiguidadeService.Linha(a, unidade, "Escalao 2", LocalDate.of(2015, 1, 1), ant, ant),
                new ListaAntiguidadeService.Linha(b, unidade, "Escalao 1", LocalDate.of(2018, 1, 1), ant, null)));
        when(listaAntiguidadeService.lista(anyInt(), any(), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(new ListaAntiguidadeService.Lista(2026, LocalDate.of(2025, 12, 31), null, true, Map.of(), List.of(grupo)));
    }

    @Test
    void aprovarCongelaAsLinhasPelaOrdemDaLista() {
        var l = service.aprovar(2026, unidade, true, "Directora", null);
        assertEquals(2, l.getLinhas().size());
        assertEquals(1, l.getLinhas().get(0).posicao());
        assertEquals(2, l.getLinhas().get(1).posicao());
        assertEquals(0, l.getLinhas().get(1).diasTotais());   // sem data de admissao: sem total
    }

    @Test
    void umaListaActivaPorServicoEAno() {
        when(repository.existeActiva(2026, unidade)).thenReturn(true);
        assertEquals(409, assertThrows(IgrpResponseStatusException.class,
                () -> service.aprovar(2026, unidade, true, "Directora", null)).getStatusCode().value());
    }

    @Test
    void afixarAvisaCadaPessoaDaLista() {
        var l = service.aprovar(2026, unidade, true, "Directora", null);
        when(repository.findById(l.getId())).thenReturn(java.util.Optional.of(l));
        service.afixar(l.getId(), LocalDate.of(2026, 2, 3), "Atrio");
        verify(notificacaoRepository, times(2)).save(any());
    }
}
