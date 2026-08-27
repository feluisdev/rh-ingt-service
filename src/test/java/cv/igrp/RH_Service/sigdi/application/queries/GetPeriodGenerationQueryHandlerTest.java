package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.EligibilitySkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluatorSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationDetailDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GetPeriodGenerationQueryHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @Mock
    private FormGenerationBatchRepository batchRepository;

    @Mock
    private TacticalActivityRepository tacticalActivityRepository;

    @InjectMocks
    private GetPeriodGenerationQueryHandler handler;

    private PaaSubmissionPeriod period(UUID id, Purpose purpose, PaaLevel type, Integer year) {
        return PaaSubmissionPeriod.reconstruct(id, purpose, type,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), "OPEN", year);
    }

    @Test
    void handleThrowsNotFoundNamingTheIdentifier() {
        UUID periodId = UUID.randomUUID();
        when(periodRepository.findById(periodId)).thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new GetPeriodGenerationQuery(periodId)));

        assertEquals(404, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(periodId.toString()));
    }

    @Test
    void handleReturnsNotGeneratedWhenPeriodHasNoBatch() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.PAA, PaaLevel.UNIT_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of());

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        assertEquals(200, response.getStatusCode().value());
        FormGenerationDetailDTO body = response.getBody();
        assertEquals("NOT_GENERATED", body.getStatus());
        assertNull(body.getBatchId());
        assertTrue(body.getCreated().isEmpty());
        assertTrue(body.getFailed().isEmpty());
        assertTrue(body.getSkipped().isEmpty());
        assertTrue(body.getPending().isEmpty());
        assertEquals(Purpose.PAA.getCode(), body.getPurpose());
        assertEquals(PaaLevel.UNIT_LEVEL.getCode(), body.getType());
        assertEquals(2026, body.getYear());
    }

    @Test
    void handleReturnsCreatedItemsWithGeneratedFormAndEvaluatorForCompletedSiadapBatch() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        UUID employeeId = UUID.randomUUID();
        UUID formId = UUID.randomUUID();
        UUID evaluatorId = UUID.randomUUID();
        FormGenerationBatchItem item = FormGenerationBatchItem.of(employeeId, "Ana Silva", UUID.randomUUID(),
                "Unidade X", FormGenerationOutcome.CREATED, formId, evaluatorId, null, null, LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.COMPLETED, 1, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(item));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(1, body.getCreated().size());
        assertTrue(body.getFailed().isEmpty());
        assertEquals(formId.toString(), body.getCreated().get(0).getGeneratedFormId());
        assertEquals(evaluatorId.toString(), body.getCreated().get(0).getEvaluatorId());
    }

    @Test
    void handlePutsFailedItemsWithNonBlankErrorMessageAndMatchingCount() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatchItem created = FormGenerationBatchItem.of(UUID.randomUUID(), "Ana", UUID.randomUUID(),
                "U", FormGenerationOutcome.CREATED, UUID.randomUUID(), UUID.randomUUID(), null, null,
                LocalDateTime.now());
        FormGenerationBatchItem failed = FormGenerationBatchItem.of(UUID.randomUUID(), "Bruno", UUID.randomUUID(),
                "U", FormGenerationOutcome.FAILED, null, null, null, "Falha ao gravar avaliação",
                LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.PARTIAL, 1, 1, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(created, failed));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(1, body.getFailed().size());
        assertEquals(body.getFailedCount(), body.getFailed().size());
        assertFalse(body.getFailed().get(0).getErrorMessage().isBlank());
    }

    @Test
    void handlePutsAlreadyExistedItemsInCreatedNotInPending() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatchItem alreadyExisted = FormGenerationBatchItem.of(UUID.randomUUID(), "Ana",
                UUID.randomUUID(), "U", FormGenerationOutcome.ALREADY_EXISTED, UUID.randomUUID(), UUID.randomUUID(),
                null, null, LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.COMPLETED, 1, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(alreadyExisted));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(1, body.getCreated().size());
        assertTrue(body.getPending().isEmpty());
    }

    @Test
    void handleResolvesSkipReasonDescriptionFromEligibilityAndEvaluatorCodesAndFallsBackToRawCode() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.PAA, PaaLevel.UNIT_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatchItem eligibilitySkip = FormGenerationBatchItem.of(null, null, UUID.randomUUID(), "U1",
                FormGenerationOutcome.SKIPPED, null, null,
                EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE.getCode(), null, LocalDateTime.now());
        FormGenerationBatchItem evaluatorSkip = FormGenerationBatchItem.of(UUID.randomUUID(), "Carla",
                UUID.randomUUID(), "U2", FormGenerationOutcome.SKIPPED, null, null,
                EvaluatorSkipReason.TOP_UNIT_HEAD.getCode(), null, LocalDateTime.now());
        FormGenerationBatchItem unknownSkip = FormGenerationBatchItem.of(UUID.randomUUID(), "Dora",
                UUID.randomUUID(), "U3", FormGenerationOutcome.SKIPPED, null, null,
                "CODIGO_DESCONHECIDO", null, LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.PAA,
                PaaLevel.UNIT_LEVEL, 2026, FormGenerationBatch.READ_ONLY, false,
                FormGenerationBatchStatus.NOTHING_TO_GENERATE, 0, 0, 3, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(eligibilitySkip, evaluatorSkip, unknownSkip));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(3, body.getSkipped().size());
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE.getDescription(),
                body.getSkipped().get(0).getSkipReasonDescription());
        assertEquals(EvaluatorSkipReason.TOP_UNIT_HEAD.getDescription(),
                body.getSkipped().get(1).getSkipReasonDescription());
        assertEquals("CODIGO_DESCONHECIDO", body.getSkipped().get(2).getSkipReasonDescription());
    }

    @Test
    void handleMarksSubmittedTrueForPaaUnitLevelPendingItemsWhoseUnitAlreadySubmitted() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.PAA, PaaLevel.UNIT_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        UUID submittedUnit = UUID.randomUUID();
        UUID pendingUnit = UUID.randomUUID();
        FormGenerationBatchItem submittedItem = FormGenerationBatchItem.of(null, null, submittedUnit, "U1",
                FormGenerationOutcome.PENDING, null, null, null, null, LocalDateTime.now());
        FormGenerationBatchItem pendingItem = FormGenerationBatchItem.of(null, null, pendingUnit, "U2",
                FormGenerationOutcome.PENDING, null, null, null, null, LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.PAA,
                PaaLevel.UNIT_LEVEL, 2026, FormGenerationBatch.READ_ONLY, false,
                FormGenerationBatchStatus.NOTHING_TO_GENERATE, 0, 0, 0, 2, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(submittedItem, pendingItem));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));
        when(tacticalActivityRepository.findOrganicUnitIdsWithActivitiesInYear(2026, PaaLevel.UNIT_LEVEL))
                .thenReturn(List.of(submittedUnit));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals("APPLICABLE", body.getSubmissionCheck());
        assertEquals(2, body.getPending().size());
        assertTrue(body.getPending().stream()
                .filter(i -> i.getUnitId().equals(submittedUnit.toString()))
                .findFirst().orElseThrow().getSubmitted());
        assertFalse(body.getPending().stream()
                .filter(i -> i.getUnitId().equals(pendingUnit.toString()))
                .findFirst().orElseThrow().getSubmitted());
    }

    @Test
    void handleReturnsNotApplicableAndNullSubmittedForPaaBscObjectivesWithoutCallingTacticalFinder() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatchItem pendingItem = FormGenerationBatchItem.of(null, null, UUID.randomUUID(), "U1",
                FormGenerationOutcome.PENDING, null, null, null, null, LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId,
                Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL, 2026, FormGenerationBatch.READ_ONLY, false,
                FormGenerationBatchStatus.NOTHING_TO_GENERATE, 0, 0, 0, 1, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(pendingItem));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals("NOT_APPLICABLE", body.getSubmissionCheck());
        assertNull(body.getPending().get(0).getSubmitted());
        verify(tacticalActivityRepository, never()).findOrganicUnitIdsWithActivitiesInYear(any(), any());
    }

    @Test
    void handleDerivesSubmittedFromOutcomeForSiadapWithoutCallingTacticalFinder() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatchItem created = FormGenerationBatchItem.of(UUID.randomUUID(), "Ana", UUID.randomUUID(),
                "U", FormGenerationOutcome.CREATED, UUID.randomUUID(), UUID.randomUUID(), null, null,
                LocalDateTime.now());
        FormGenerationBatchItem failed = FormGenerationBatchItem.of(UUID.randomUUID(), "Bruno", UUID.randomUUID(),
                "U", FormGenerationOutcome.FAILED, null, null, null, "erro", LocalDateTime.now());
        FormGenerationBatch batch = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.PARTIAL, 1, 1, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of(created, failed));
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(batch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals("APPLICABLE", body.getSubmissionCheck());
        assertTrue(body.getCreated().get(0).getSubmitted());
        assertFalse(body.getFailed().get(0).getSubmitted());
        verify(tacticalActivityRepository, never()).findOrganicUnitIdsWithActivitiesInYear(any(), any());
    }

    @Test
    void handleSelectsMostRecentNonDryRunBatchWhenSeveralExist() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        FormGenerationBatch dryRunNewest = FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId,
                Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, true,
                FormGenerationBatchStatus.DRY_RUN, 0, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of());
        UUID realBatchId = UUID.randomUUID();
        FormGenerationBatch realBatch = FormGenerationBatch.reconstruct(realBatchId, periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.COMPLETED, 0, 0, 0, 0, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1), "scheduler:period-opening", List.of());
        // findByPeriodId contract: generatedAt DESC -- dry-run (mais recente) primeiro.
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(dryRunNewest, realBatch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(realBatchId.toString(), body.getBatchId());
        assertFalse(body.isDryRun());
    }

    @Test
    void handleSelectsMostRecentDryRunBatchAndMarksDryRunWhenOnlySimulationsExist() {
        UUID periodId = UUID.randomUUID();
        PaaSubmissionPeriod period = period(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026);
        when(periodRepository.findById(periodId)).thenReturn(Optional.of(period));

        UUID onlyBatchId = UUID.randomUUID();
        FormGenerationBatch onlyBatch = FormGenerationBatch.reconstruct(onlyBatchId, periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, true,
                FormGenerationBatchStatus.DRY_RUN, 0, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(),
                "scheduler:period-opening", List.of());
        when(batchRepository.findByPeriodId(periodId)).thenReturn(List.of(onlyBatch));

        ResponseEntity<FormGenerationDetailDTO> response = handler.handle(new GetPeriodGenerationQuery(periodId));

        FormGenerationDetailDTO body = response.getBody();
        assertEquals(onlyBatchId.toString(), body.getBatchId());
        assertTrue(body.isDryRun());
    }
}
