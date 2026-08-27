package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluatorSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes de {@link SiadapFormGenerator}: cria, salta, reconhece duplicados, falha por
 * colaborador e simula, sem nunca passar por {@code CreateSiadapEvaluationCommandHandler}
 * (D-10, 119-03-PLAN.md). Identificadores gerados sempre por {@link UUID#randomUUID()}.
 */
@ExtendWith(MockitoExtension.class)
class SiadapFormGeneratorTest {

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapConfigRepository configRepository;

    @Mock
    private EvaluatorResolver evaluatorResolver;

    @InjectMocks
    private SiadapFormGenerator generator;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 27, 10, 0);
    private static final Integer YEAR = 2026;

    @Test
    void colaboradorElegivelSemAvaliacaoComAvaliadorDerivadoCriaEDevolveItemCreated() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(1, items.size());
        FormGenerationBatchItem item = items.get(0);
        assertEquals(FormGenerationOutcome.CREATED, item.getOutcome());
        assertEquals(evaluatorId, item.getEvaluatorId());

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        SiadapEvaluation saved = captor.getValue();
        assertEquals(UUID.fromString(saved.getId().getStringValor()), item.getGeneratedFormId());
    }

    @Test
    void avaliacaoCriadaLevaOOrganicUnitIdDoGrupoElegivelENaoAUnidadeAtualDoColaborador() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        generator.generate(eligible, YEAR, false, NOW);

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(unitId.toString(), captor.getValue().getOrganicUnitId());
    }

    @Test
    void pesosVemDoSiadapConfigDoAnoQuandoExiste() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();
        SiadapConfig config = SiadapConfig.reconstruct(UUID.randomUUID(), YEAR, new BigDecimal("4.5"),
                new BigDecimal("4.8"), new BigDecimal("10"), new BigDecimal("20"), 5,
                new BigDecimal("70"), new BigDecimal("30"));

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.of(config));
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        generator.generate(eligible, YEAR, false, NOW);

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(new BigDecimal("70"), captor.getValue().getResultsWeight());
        assertEquals(new BigDecimal("30"), captor.getValue().getCompetenciesWeight());
    }

    @Test
    void pesosSaoSessentaEQuarentaQuandoNaoExisteSiadapConfig() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        generator.generate(eligible, YEAR, false, NOW);

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(new BigDecimal("60"), captor.getValue().getResultsWeight());
        assertEquals(new BigDecimal("40"), captor.getValue().getCompetenciesWeight());
    }

    @Test
    void findByFiscalYearEChamadoUmaVezPorInvocacaoMesmoComDezColaboradores() {
        UUID unitId = UUID.randomUUID();
        List<EligibleResponsibleDTO> responsibles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UUID employeeId = UUID.randomUUID();
            UUID evaluatorId = UUID.randomUUID();
            responsibles.add(responsible(employeeId, "Colaborador " + i));
            when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
            when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());
        }
        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(unitId.toString());
        group.setUnitName("Unidade A");
        group.setUnitAcronym("UA");
        group.setResponsibles(responsibles);

        EligibleResponsiblesDTO eligible = eligible(List.of(group), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(10, items.size());
        verify(configRepository, times(1)).findByFiscalYear(YEAR);
    }

    @Test
    void colaboradorComAvaliacaoExistenteDevolveAlreadyExistedENuncaChamaSave() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();
        SiadapEvaluation existing = SiadapEvaluation.create(employeeId.toString(), YEAR, unitId.toString(),
                evaluatorId.toString(), new BigDecimal("60"), new BigDecimal("40"));

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.of(existing));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(1, items.size());
        FormGenerationBatchItem item = items.get(0);
        assertEquals(FormGenerationOutcome.ALREADY_EXISTED, item.getOutcome());
        assertEquals(UUID.fromString(existing.getId().getStringValor()), item.getGeneratedFormId());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void colaboradorCujoAvaliadorNaoSeDerivaDevolveSkippedComSkipReasonENuncaChamaSave() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId))
                .thenReturn(EvaluatorResolver.EvaluatorResolution.skip(EvaluatorSkipReason.UNIT_WITHOUT_RESPONSIBLE));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(1, items.size());
        FormGenerationBatchItem item = items.get(0);
        assertEquals(FormGenerationOutcome.SKIPPED, item.getOutcome());
        assertEquals(EvaluatorSkipReason.UNIT_WITHOUT_RESPONSIBLE.getCode(), item.getSkipReason());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void saveALancarParaUmColaboradorDevolveFailedEOsSeguintesContinuamASerProcessados() {
        UUID failingEmployeeId = UUID.randomUUID();
        UUID okEmployeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(any(), eq(unitId))).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(anyString(), eq(YEAR))).thenReturn(Optional.empty());
        when(evaluationRepository.save(any()))
                .thenThrow(new RuntimeException("falha de escrita simulada"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(unitId.toString());
        group.setUnitName("Unidade A");
        group.setUnitAcronym("UA");
        group.setResponsibles(List.of(
                responsible(failingEmployeeId, "Colaborador que falha"),
                responsible(okEmployeeId, "Colaborador que passa")));

        EligibleResponsiblesDTO eligible = eligible(List.of(group), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(2, items.size());
        FormGenerationBatchItem first = items.get(0);
        assertEquals(FormGenerationOutcome.FAILED, first.getOutcome());
        assertFalse(first.getErrorMessage() == null || first.getErrorMessage().isBlank());

        FormGenerationBatchItem second = items.get(1);
        assertEquals(FormGenerationOutcome.CREATED, second.getOutcome());
    }

    @Test
    void unidadeSaltadaPelaFase116DevolveItemSkippedComEmployeeIdNulo() {
        UUID unitId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());

        SkippedUnitDTO skippedUnit = new SkippedUnitDTO();
        skippedUnit.setUnitId(unitId.toString());
        skippedUnit.setUnitName("Unidade Sem Responsavel");
        skippedUnit.setReason("UNIT_WITHOUT_RESPONSIBLE");
        skippedUnit.setReasonDescription("Unidade orgânica sem responsável definido");

        EligibleResponsiblesDTO eligible = eligible(List.of(), List.of(skippedUnit));

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(1, items.size());
        FormGenerationBatchItem item = items.get(0);
        assertEquals(FormGenerationOutcome.SKIPPED, item.getOutcome());
        assertNull(item.getEmployeeId());
        assertEquals(unitId, item.getUnitId());
        assertEquals("Unidade Sem Responsavel", item.getUnitName());
        assertEquals("UNIT_WITHOUT_RESPONSIBLE", item.getSkipReason());
    }

    @Test
    void emModoDeSimulacaoDevolveWouldCreateENuncaChamaSave() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.empty());

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, true, NOW);

        assertEquals(1, items.size());
        assertEquals(FormGenerationOutcome.WOULD_CREATE, items.get(0).getOutcome());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void emModoDeSimulacaoODuplicadoContinuaADarAlreadyExisted() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();
        SiadapEvaluation existing = SiadapEvaluation.create(employeeId.toString(), YEAR, unitId.toString(),
                evaluatorId.toString(), new BigDecimal("60"), new BigDecimal("40"));

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());
        when(evaluatorResolver.resolve(employeeId, unitId)).thenReturn(EvaluatorResolver.EvaluatorResolution.of(evaluatorId));
        when(evaluationRepository.findByEmployeeAndYear(employeeId.toString(), YEAR)).thenReturn(Optional.of(existing));

        EligibleResponsiblesDTO eligible = eligible(List.of(
                group(unitId, "Unidade A", "UA", responsible(employeeId, "Colaborador A"))), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, true, NOW);

        assertEquals(1, items.size());
        assertEquals(FormGenerationOutcome.ALREADY_EXISTED, items.get(0).getOutcome());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void employeeIdDoDtoQueNaoSejaUuidValidoDevolveFailedComMensagemQueNomeiaOIdentificador() {
        UUID unitId = UUID.randomUUID();
        String badEmployeeId = "nao-e-um-uuid";

        when(configRepository.findByFiscalYear(YEAR)).thenReturn(Optional.empty());

        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(unitId.toString());
        group.setUnitName("Unidade A");
        group.setUnitAcronym("UA");
        EligibleResponsibleDTO badResponsible = new EligibleResponsibleDTO();
        badResponsible.setEmployeeId(badEmployeeId);
        badResponsible.setEmployeeName("Colaborador Mal Formado");
        group.setResponsibles(List.of(badResponsible));

        EligibleResponsiblesDTO eligible = eligible(List.of(group), List.of());

        List<FormGenerationBatchItem> items = generator.generate(eligible, YEAR, false, NOW);

        assertEquals(1, items.size());
        FormGenerationBatchItem item = items.get(0);
        assertEquals(FormGenerationOutcome.FAILED, item.getOutcome());
        assertTrue(item.getErrorMessage().contains(badEmployeeId));
        verify(evaluationRepository, never()).save(any());
    }

    // ---- helpers ----

    private EligibleResponsiblesDTO eligible(List<EligibleUnitGroupDTO> groups, List<SkippedUnitDTO> skipped) {
        EligibleResponsiblesDTO dto = new EligibleResponsiblesDTO();
        dto.setGroups(groups);
        dto.setSkipped(skipped);
        return dto;
    }

    private EligibleUnitGroupDTO group(UUID unitId, String unitName, String unitAcronym, EligibleResponsibleDTO... responsibles) {
        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(unitId.toString());
        group.setUnitName(unitName);
        group.setUnitAcronym(unitAcronym);
        group.setResponsibles(List.of(responsibles));
        return group;
    }

    private EligibleResponsibleDTO responsible(UUID employeeId, String employeeName) {
        EligibleResponsibleDTO dto = new EligibleResponsibleDTO();
        dto.setEmployeeId(employeeId.toString());
        dto.setEmployeeName(employeeName);
        return dto;
    }
}
