package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * A unidade onde exerce funcoes numa data: a da afectacao principal que cobria essa data (nao a de
 * hoje); sem nenhuma, a actual; a mobilidade interna manda; a externa da nenhuma.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnidadeDeExercicioServiceTest {

    @Mock private MobilidadeService mobilidadeService;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;
    @InjectMocks private UnidadeDeExercicioService service;

    private final FuncionarioId pessoa = FuncionarioId.gerarNovo();
    private final UUID unidadeAntiga = UUID.randomUUID();
    private final UUID unidadeNova = UUID.randomUUID();
    private Assignment antiga;
    private Assignment nova;

    private Assignment afectacao(UUID unidade, LocalDate inicio, LocalDate fim, boolean principal) {
        UUID lugar = UUID.randomUUID();
        var p = mock(Position.class);
        when(p.getUnidadeOrganicaId()).thenReturn(unidade);
        when(positionRepository.findById(PositionId.from(lugar))).thenReturn(Optional.of(p));
        var a = mock(Assignment.class);
        when(a.getPositionId()).thenReturn(lugar);
        when(a.getDataInicio()).thenReturn(inicio);
        when(a.getDataFim()).thenReturn(fim);
        when(a.isPrincipal()).thenReturn(principal);
        return a;
    }

    @BeforeEach
    void base() {
        // Transferido a 1 de Julho: ate 30 de Junho na antiga, desde entao na nova.
        antiga = afectacao(unidadeAntiga, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 6, 30), true);
        nova = afectacao(unidadeNova, LocalDate.of(2026, 7, 1), null, true);
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(pessoa)).thenReturn(List.of(nova, antiga));
        when(assignmentRepository.findCurrentPrincipalByFuncionario(pessoa)).thenReturn(Optional.of(nova));
        when(mobilidadeService.mobilidadeEmVigor(any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void umaDataPassadaUsaAUnidadeDessaAltura() {
        assertEquals(unidadeAntiga, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2026, 6, 30)));
        assertEquals(unidadeNova, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2026, 7, 1)));
        assertEquals(unidadeNova, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2027, 1, 1)));   // o futuro
    }

    @Test
    void semAfectacaoQueCubraADataValeAActual() {
        assertEquals(unidadeNova, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2019, 5, 1)));
    }

    @Test
    void umaSubstituicaoNoutraUnidadeNaoContaSoAPrincipal() {
        var substituicao = afectacao(UUID.randomUUID(), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), false);
        when(assignmentRepository.findAllByFuncionarioOrderByDataInicioDesc(pessoa)).thenReturn(List.of(substituicao, nova, antiga));
        assertEquals(unidadeNova, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2026, 8, 10)));
    }

    @Test
    void aMobilidadeMandaAInternaDaODestinoAExternaNenhuma() {
        var interna = mock(LicencaMobilidade.class);
        UUID destino = UUID.randomUUID();
        when(interna.isDestinoInterno()).thenReturn(true);
        when(interna.getDestinationUnitId()).thenReturn(destino);
        when(mobilidadeService.mobilidadeEmVigor(pessoa, LocalDate.of(2026, 3, 2))).thenReturn(Optional.of(interna));
        assertEquals(destino, service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2026, 3, 2)));

        var externa = mock(LicencaMobilidade.class);
        when(externa.isDestinoInterno()).thenReturn(false);
        when(mobilidadeService.mobilidadeEmVigor(pessoa, LocalDate.of(2026, 3, 3))).thenReturn(Optional.of(externa));
        assertNull(service.unidadeOndeExerceFuncoes(pessoa, LocalDate.of(2026, 3, 3)));
    }
}
