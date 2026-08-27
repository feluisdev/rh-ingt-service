package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes de {@link PeriodFormGenerationService}: o despacho das sete combinações de
 * {@code (Purpose, PaaLevel)} e os cinco estados de {@link FormGenerationBatchStatus}.
 * Identificadores gerados sempre por {@link UUID#randomUUID()}.
 */
@ExtendWith(MockitoExtension.class)
class PeriodFormGenerationServiceTest {

    @Mock
    private EligibleResponsiblesResolver eligibleResponsiblesResolver;

    @Mock
    private SiadapFormGenerator siadapFormGenerator;

    @Mock
    private FormGenerationBatchRepository formGenerationBatchRepository;

    @InjectMocks
    private PeriodFormGenerationService service;

    private static final Integer YEAR = 2026;

    private PaaSubmissionPeriod period(Purpose purpose, PaaLevel type) {
        return PaaSubmissionPeriod.reconstruct(UUID.randomUUID(), purpose, type,
                LocalDate.of(YEAR, 1, 1), LocalDate.of(YEAR, 12, 31), "OPEN", YEAR);
    }

    private EligibleResponsiblesDTO eligibleWithOneResponsibleAndOneSkipped() {
        EligibleResponsibleDTO responsible = new EligibleResponsibleDTO();
        responsible.setEmployeeId(UUID.randomUUID().toString());
        responsible.setEmployeeName("Colaborador A");

        EligibleUnitGroupDTO group = new EligibleUnitGroupDTO();
        group.setUnitId(UUID.randomUUID().toString());
        group.setUnitName("Unidade A");
        group.setUnitAcronym("UA");
        group.setResponsibles(List.of(responsible));

        SkippedUnitDTO skippedUnit = new SkippedUnitDTO();
        skippedUnit.setUnitId(UUID.randomUUID().toString());
        skippedUnit.setUnitName("Unidade Saltada");
        skippedUnit.setReason("UNIT_WITHOUT_RESPONSIBLE");

        EligibleResponsiblesDTO dto = new EligibleResponsiblesDTO();
        dto.setGroups(List.of(group));
        dto.setSkipped(List.of(skippedUnit));
        return dto;
    }

    private FormGenerationBatchItem createdItem() {
        return FormGenerationBatchItem.of(UUID.randomUUID(), "Colaborador A", UUID.randomUUID(), "Unidade A",
                FormGenerationOutcome.CREATED, UUID.randomUUID(), UUID.randomUUID(), null, null, LocalDateTime.now());
    }

    private FormGenerationBatchItem failedItem() {
        return FormGenerationBatchItem.of(UUID.randomUUID(), "Colaborador B", UUID.randomUUID(), "Unidade A",
                FormGenerationOutcome.FAILED, null, null, null, "erro simulado", LocalDateTime.now());
    }

    @Test
    void periodoSiadapIndividualLevelDespachaCreatesFormsEChamaOGeradorSiadap() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();
        List<FormGenerationBatchItem> generatorItems = List.of(createdItem());

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(generatorItems);
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, false);

        verify(siadapFormGenerator).generate(any(), anyInt(), anyBoolean(), any());
        assertEquals(FormGenerationBatch.CREATES_FORMS, batch.getGenerationMode());
        assertEquals(1, batch.getItems().size());
        assertEquals(FormGenerationOutcome.CREATED, batch.getItems().get(0).getOutcome());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> readOnlyCombinations() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(Purpose.PAA, PaaLevel.UNIT_LEVEL),
                org.junit.jupiter.params.provider.Arguments.of(Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL),
                org.junit.jupiter.params.provider.Arguments.of(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL),
                org.junit.jupiter.params.provider.Arguments.of(Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL),
                org.junit.jupiter.params.provider.Arguments.of(Purpose.SIADAP_SELF_EVAL, PaaLevel.INDIVIDUAL_LEVEL),
                org.junit.jupiter.params.provider.Arguments.of(Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL)
        );
    }

    @ParameterizedTest
    @MethodSource("readOnlyCombinations")
    void asSeisCombinacoesQueNaoCriamDespachamReadOnlyENuncaChamamOGeradorSiadap(Purpose purpose, PaaLevel type) {
        PaaSubmissionPeriod period = period(purpose, type);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, false);

        verify(siadapFormGenerator, never()).generate(any(), anyInt(), anyBoolean(), any());
        assertEquals(FormGenerationBatch.READ_ONLY, batch.getGenerationMode());
        assertEquals(2, batch.getItems().size());
        assertTrue(batch.getItems().stream().anyMatch(item -> item.getOutcome() == FormGenerationOutcome.PENDING));
        assertTrue(batch.getItems().stream().anyMatch(item -> item.getOutcome() == FormGenerationOutcome.SKIPPED));
        assertEquals(FormGenerationBatchStatus.NOTHING_TO_GENERATE, batch.getStatus());
    }

    @Test
    void loteSemFalhasEmCreatesFormsFicaCompleted() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(createdItem()));
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, false);

        assertEquals(FormGenerationBatchStatus.COMPLETED, batch.getStatus());
    }

    @Test
    void loteComPeloMenosUmaFalhaEUmaCriacaoFicaPartial() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any()))
                .thenReturn(List.of(createdItem(), failedItem()));
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, false);

        assertEquals(FormGenerationBatchStatus.PARTIAL, batch.getStatus());
    }

    @Test
    void loteSoComFalhasFicaFailed() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(failedItem()));
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, false);

        assertEquals(FormGenerationBatchStatus.FAILED, batch.getStatus());
    }

    @Test
    void loteEmSimulacaoFicaDryRun() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();
        FormGenerationBatchItem wouldCreate = FormGenerationBatchItem.of(UUID.randomUUID(), "Colaborador A",
                UUID.randomUUID(), "Unidade A", FormGenerationOutcome.WOULD_CREATE, null, UUID.randomUUID(), null, null,
                LocalDateTime.now());

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(wouldCreate));
        when(formGenerationBatchRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FormGenerationBatch batch = service.generateFor(period, true);

        assertEquals(FormGenerationBatchStatus.DRY_RUN, batch.getStatus());
        assertTrue(batch.isDryRun());
    }

    @Test
    void generatedByEOAutorDeSistemaActivoQuandoHaAmbito() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(createdItem()));
        ArgumentCaptor<FormGenerationBatch> captor = ArgumentCaptor.forClass(FormGenerationBatch.class);
        when(formGenerationBatchRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        SystemAuditor.runAs("scheduler:period-opening", () -> service.generateFor(period, false));

        assertEquals("scheduler:period-opening", captor.getValue().getGeneratedBy());
    }

    @Test
    void generatedByESystemQuandoNaoHaAmbitoActivo() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(createdItem()));
        ArgumentCaptor<FormGenerationBatch> captor = ArgumentCaptor.forClass(FormGenerationBatch.class);
        when(formGenerationBatchRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.generateFor(period, false);

        assertEquals("system", captor.getValue().getGeneratedBy());
    }

    @Test
    void oLoteEGravadoUmaVezDepoisDeFinishENuncaAntes() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);
        EligibleResponsiblesDTO eligible = eligibleWithOneResponsibleAndOneSkipped();

        when(eligibleResponsiblesResolver.resolve(period)).thenReturn(eligible);
        when(siadapFormGenerator.generate(any(), anyInt(), anyBoolean(), any())).thenReturn(List.of(createdItem()));
        ArgumentCaptor<FormGenerationBatch> captor = ArgumentCaptor.forClass(FormGenerationBatch.class);
        when(formGenerationBatchRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.generateFor(period, false);

        verify(formGenerationBatchRepository, times(1)).save(any());
        assertEquals(FormGenerationBatchStatus.COMPLETED, captor.getValue().getStatus());
        assertTrue(captor.getValue().getFinishedAt() != null);
    }

    @Test
    void excepcaoDoEligibleResponsiblesResolverPropagaENaoHaLoteAGravar() {
        PaaSubmissionPeriod period = period(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL);

        when(eligibleResponsiblesResolver.resolve(period)).thenThrow(new RuntimeException("falha na Fase 116"));

        assertThrows(RuntimeException.class, () -> service.generateFor(period, false));
        verify(formGenerationBatchRepository, never()).save(any());
    }
}
