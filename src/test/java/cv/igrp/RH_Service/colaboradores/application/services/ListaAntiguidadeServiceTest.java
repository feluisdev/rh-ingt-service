package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.queries.GetListaAntiguidadeCsvQueryHandler;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * DL n.o 3/2010, arts. 69.o e 70.o: por cargo (carreira e categoria, a mais alta primeiro; fora da grelha
 * no fim) e, em cada cargo, pela antiguidade no cargo; o inicio no cargo e o da cadeia seguida na mesma
 * categoria (a progressao nao corta; uma interrupcao corta).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListaAntiguidadeServiceTest {

    private static final LocalDate REF = LocalDate.of(2025, 12, 31);

    @Mock private QuemEstaNoServico quemEstaNoServico;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private JobRepository jobRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private AntiguidadeService antiguidadeService;

    private ListaAntiguidadeService service;
    private final UUID unidadeId = UUID.randomUUID();
    private final Map<UUID, QuemEstaNoServico.Colocacao> colocacoes = new LinkedHashMap<>();
    private final Map<FuncionarioId, List<Assignment>> historicos = new HashMap<>();
    private final List<Funcionario> pessoas = new ArrayList<>();

    private UUID categoria(String nome, int ordem, CareerId carreira) {
        UUID id = UUID.randomUUID();
        var c = mock(Category.class);
        when(c.getName()).thenReturn(nome);
        when(c.getOrdemProgressao()).thenReturn(ordem);
        when(c.getCareerId()).thenReturn(carreira);
        when(categoryRepository.findById(CategoryId.from(id))).thenReturn(Optional.of(c));
        return id;
    }

    private UUID escalao(UUID categoria, String nome) {
        UUID id = UUID.randomUUID();
        var g = mock(Grade.class);
        when(g.getCategoryId()).thenReturn(CategoryId.from(categoria));
        when(g.getName()).thenReturn(nome);
        when(gradeRepository.findById(GradeId.from(id))).thenReturn(Optional.of(g));
        return id;
    }

    private Assignment afectacao(FuncionarioId quem, UUID escalao, UUID lugar, LocalDate de, LocalDate ate) {
        var a = mock(Assignment.class);
        when(a.getId()).thenReturn(AssignmentId.gerarNovo());
        when(a.getFuncionarioId()).thenReturn(quem);
        when(a.isPrincipal()).thenReturn(true);
        when(a.getGradeId()).thenReturn(escalao);
        when(a.getPositionId()).thenReturn(lugar);
        when(a.getDataInicio()).thenReturn(de);
        when(a.getDataFim()).thenReturn(ate);
        historicos.computeIfAbsent(quem, k -> new ArrayList<>()).add(a);
        return a;
    }

    private FuncionarioId pessoa(String nome, LocalDate admissao, Assignment actualNaRef) {
        var f = mock(Funcionario.class);
        FuncionarioId id = actualNaRef.getFuncionarioId();
        when(f.getId()).thenReturn(id);
        when(f.getNomeCompleto()).thenReturn(nome);
        when(f.getNumeroFuncionario()).thenReturn(nome.substring(0, 3));
        when(f.getDataAdmissao()).thenReturn(admissao);
        pessoas.add(f);
        colocacoes.put(id.getValor(), new QuemEstaNoServico.Colocacao(actualNaRef, unidadeId));
        when(antiguidadeService.calcular(eq(id), any())).thenAnswer(i ->
                CalculadoraAntiguidade.calcular(admissao, i.getArgument(1), List.of()));
        return id;
    }

    @BeforeEach
    void base() {
        service = new ListaAntiguidadeService(quemEstaNoServico, funcionarioRepository, assignmentRepository, positionRepository,
                jobRepository, gradeRepository, categoryRepository, careerRepository, antiguidadeService) {
            @Override protected LocalDate hoje() { return LocalDate.of(2026, 9, 24); }
        };
        var u = OrganizationalUnit.reconstruir(OrganizationalUnitId.from(unidadeId), "SRH", "Servico de RH", "SRH",
                null, null, null, null, null, true);
        when(quemEstaNoServico.unidades(unidadeId, true)).thenReturn(List.of(u));
        when(quemEstaNoServico.colocacoes(any(), eq(REF), eq(REF))).thenReturn(colocacoes);
        when(funcionarioRepository.findAllByIds(any())).thenReturn(pessoas);
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(any()))
                .thenAnswer(i -> historicos.getOrDefault((FuncionarioId) i.getArgument(0), List.of()));
        when(antiguidadeService.calcularDesde(any(), any(), any())).thenAnswer(i ->
                CalculadoraAntiguidade.calcular(i.getArgument(1), i.getArgument(2), List.of()));
        var carreira = CareerId.gerarNovo();
        var c = mock(Career.class);
        when(c.getName()).thenReturn("Tecnica");
        when(careerRepository.findById(carreira)).thenReturn(Optional.of(c));
        // As categorias sao criadas aqui para ficarem ligadas a carreira.
        tecnicoIds(carreira);
    }

    private UUID catTecnico, catPrincipal, e1, e2, p1;

    private void tecnicoIds(CareerId carreira) {
        catTecnico = categoria("Tecnico", 1, carreira);
        catPrincipal = categoria("Tecnico Principal", 2, carreira);
        e1 = escalao(catTecnico, "T-1");
        e2 = escalao(catPrincipal, "TP-1");
        p1 = escalao(catPrincipal, "TP-2");
    }

    @Test
    void porCargoEPorAntiguidadeNoCargo() {
        // Maria: Tecnico desde 2020, promovida a Principal em 2024-03-01, progrediu em 2025-07-01.
        var maria = FuncionarioId.gerarNovo();
        afectacao(maria, e1, UUID.randomUUID(), LocalDate.of(2020, 1, 1), LocalDate.of(2024, 2, 29));
        afectacao(maria, e2, UUID.randomUUID(), LocalDate.of(2024, 3, 1), LocalDate.of(2025, 6, 30));
        var mariaHoje = afectacao(maria, p1, UUID.randomUUID(), LocalDate.of(2025, 7, 1), null);
        pessoa("Maria", LocalDate.of(2020, 1, 1), mariaHoje);
        // Joana: Principal desde 2022.
        var joana = FuncionarioId.gerarNovo();
        var joanaHoje = afectacao(joana, e2, UUID.randomUUID(), LocalDate.of(2022, 1, 1), null);
        pessoa("Joana", LocalDate.of(2022, 1, 1), joanaHoje);
        // Pedro: Tecnico de 2015 a 2018, saiu, voltou em 2021: a interrupcao corta a cadeia.
        var pedro = FuncionarioId.gerarNovo();
        afectacao(pedro, e1, UUID.randomUUID(), LocalDate.of(2015, 1, 1), LocalDate.of(2018, 12, 31));
        var pedroHoje = afectacao(pedro, e1, UUID.randomUUID(), LocalDate.of(2021, 1, 1), null);
        pessoa("Pedro", LocalDate.of(2015, 1, 1), pedroHoje);
        // Francisco: fora da grelha, no cargo de Motorista.
        var francisco = FuncionarioId.gerarNovo();
        UUID lugar = UUID.randomUUID();
        UUID job = UUID.randomUUID();
        var p = mock(Position.class);
        when(p.getJobId()).thenReturn(job);
        when(positionRepository.findById(PositionId.from(lugar))).thenReturn(Optional.of(p));
        var j = mock(Job.class);
        when(j.getName()).thenReturn("Motorista");
        when(jobRepository.findById(JobId.from(job))).thenReturn(Optional.of(j));
        var franciscoHoje = afectacao(francisco, null, lugar, LocalDate.of(2019, 5, 1), null);
        pessoa("Francisco", LocalDate.of(2019, 5, 1), franciscoHoje);

        var l = service.lista(2026, unidadeId, true);

        assertEquals(REF, l.referencia());
        assertEquals(3, l.grupos().size());
        assertEquals("Tecnico Principal", l.grupos().get(0).categoria());   // a categoria mais alta primeiro
        assertEquals("Tecnico", l.grupos().get(1).categoria());
        assertTrue(l.grupos().get(2).foraDeGrelha());
        assertEquals("Motorista", l.grupos().get(2).categoria());

        var principal = l.grupos().get(0).linhas();
        assertEquals("Joana", principal.get(0).funcionario().getNomeCompleto());   // mais antiga no cargo
        assertEquals("Maria", principal.get(1).funcionario().getNomeCompleto());
        assertEquals(LocalDate.of(2024, 3, 1), principal.get(1).inicioNoCargo()); // a progressao nao cortou
        assertEquals("TP-2", principal.get(1).escalao());

        var pedroLinha = l.grupos().get(1).linhas().get(0);
        assertEquals(LocalDate.of(2021, 1, 1), pedroLinha.inicioNoCargo());      // a interrupcao cortou
        assertEquals(11, pedroLinha.total().anos());                              // o total conta desde a admissao (2015)
        assertEquals(5, pedroLinha.noCargo().anos());
    }

    @Test
    void csvUmaLinhaPorFuncionarioPelaOrdemDaLista() {
        var joana = FuncionarioId.gerarNovo();
        pessoa("Joana", LocalDate.of(2022, 1, 1), afectacao(joana, e2, UUID.randomUUID(), LocalDate.of(2022, 1, 1), null));
        String csv = GetListaAntiguidadeCsvQueryHandler.csv(service.lista(2026, unidadeId, true));
        String[] linhas = csv.split("\r\n");
        assertTrue(linhas[0].startsWith("﻿ano;referencia;carreira;categoria;posicao"));
        assertEquals(2, linhas.length);
        assertTrue(linhas[1].startsWith("2026;2025-12-31;Tecnica;Tecnico Principal;1;Joa;Joana;"), linhas[1]);
    }

    @Test
    void anoFuturoOuSemUnidadeE422() {
        var futuro = assertThrows(IgrpResponseStatusException.class, () -> service.lista(2027, unidadeId, true));
        assertEquals(422, futuro.getStatusCode().value());
        var semUnidade = assertThrows(IgrpResponseStatusException.class, () -> service.lista(2026, null, true));
        assertEquals(422, semUnidade.getStatusCode().value());
    }
}
