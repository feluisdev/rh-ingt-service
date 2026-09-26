package cv.igrp.RH_Service.recrutamento.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import cv.igrp.RH_Service.colaboradores.application.services.ImpedimentosDisciplinares;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.colaboradores.application.services.PublicacoesService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.models.ConcursoTest;
import cv.igrp.RH_Service.recrutamento.domain.repository.ConcursoRepository;
import cv.igrp.RH_Service.recrutamento.domain.service.ClassificacaoConcurso;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** BR-CNC: candidaturas conforme a modalidade; provimento pela ordem da lista, com a quota à frente e descontada. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConcursoServiceTest {

    @Mock private ConcursoRepository repository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private PositionOccupancyPort ocupacao;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PublicacoesService publicacoes;
    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private ImpedimentosDisciplinares impedimentos;
    private ConcursoService service;
    private LocalDate hoje = ConcursoTest.HOJE;

    @BeforeEach
    void setUp() {
        service = new ConcursoService(repository, categoryRepository, positionRepository, ocupacao, funcionarioRepository, publicacoes,
                new Notificador(notificacaoRepository), impedimentos) {
            @Override LocalDate hoje() { return hoje; }
        };
        when(repository.save(any(Candidatura.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));
        when(ocupacao.ocupados(any())).thenReturn(Set.of());
    }

    private Concurso aberto(Concurso.Modalidade modalidade, int lugares, Integer quota) {
        var c = ConcursoTest.pronto(modalidade, null, lugares, quota);
        c.abrir(hoje);
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void internoRestritoSoParaOsDaCasa() {
        var c = aberto(Concurso.Modalidade.INTERNO_RESTRITO, 1, null);
        var d = new ConcursoService.Candidato("Rui", "CNI1", null, null, null, null, false, null, true);
        assertThrows(IgrpResponseStatusException.class, () -> service.candidatar(c.getId(), d));
    }

    @Test
    void internoExigeVinculoAAdministracao() {
        var c = aberto(Concurso.Modalidade.INTERNO, 1, null);
        assertThrows(IgrpResponseStatusException.class, () -> service.candidatar(c.getId(),
                new ConcursoService.Candidato("Rui", "CNI1", null, null, null, null, false, null, false)));
        var x = service.candidatar(c.getId(), new ConcursoService.Candidato("Rui", "cni1", null, null, null, null, false, null, true));
        assertEquals("CNI1", x.getDocumento());
    }

    @Test
    void naoConcorreDuasVezes() {
        var c = aberto(Concurso.Modalidade.EXTERNO, 1, 0);
        when(repository.existeCandidatura(c.getId(), "CNI1", null)).thenReturn(true);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> service.candidatar(c.getId(),
                new ConcursoService.Candidato("Rui", "cni1", null, null, null, null, false, null, false)));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void provePelaOrdemComAQuotaAFrente() {
        var c = aberto(Concurso.Modalidade.EXTERNO, 2, 1);
        var a = ConcursoTest.candidato(c, "A", false, hoje, 16, 16, 16, 16);
        var d = ConcursoTest.candidato(c, "D", true, hoje, 11, 11, 11, 11);
        var e = ConcursoTest.candidato(c, "E", false, hoje, 12, 12, 12, 12);
        var todas = new ArrayList<>(List.of(a, d, e));
        ClassificacaoConcurso.classificar(c, todas);
        c.encerrarCandidaturas(hoje.plusDays(16));
        c.iniciarAvaliacao(false);
        c.listaProvisoria(false);
        c.homologar("Despacho 1/2026", hoje.plusDays(30));
        hoje = hoje.plusDays(40);
        when(repository.findCandidaturas(c.getId())).thenReturn(todas);
        for (var x : todas) when(repository.findCandidatura(x.getId())).thenReturn(Optional.of(x));
        var l1 = c.getLugares().get(0);
        var l2 = c.getLugares().get(1);

        // A, 1.º na lista, tem de esperar pela quota: segue-se D.
        assertThrows(IgrpResponseStatusException.class, () -> service.prover(c.getId(), a.getId(), l1));
        service.prover(c.getId(), d.getId(), l1);
        assertEquals(Candidatura.Estado.PROVIDA, d.getEstado());

        // Quota gasta: agora é A, não E; e o Lugar já provido não se repete.
        assertThrows(IgrpResponseStatusException.class, () -> service.prover(c.getId(), e.getId(), l2));
        assertThrows(IgrpResponseStatusException.class, () -> service.prover(c.getId(), a.getId(), l1));
        service.prover(c.getId(), a.getId(), l2);
        assertEquals(l2, a.getLugarProvidoId());
    }

    @Test
    void naoProveAntesDaHomologacao() {
        var c = aberto(Concurso.Modalidade.INTERNO, 1, null);
        var ex = assertThrows(IgrpResponseStatusException.class,
                () -> service.prover(c.getId(), cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId.gerarNovo(), c.getLugares().get(0)));
        assertEquals(422, ex.getStatusCode().value());
    }
}
