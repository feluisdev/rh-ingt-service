package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationSummaryDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GetPeriodGenerationSummariesQueryHandlerTest {

    @Mock
    private FormGenerationBatchRepository batchRepository;

    @InjectMocks
    private GetPeriodGenerationSummariesQueryHandler handler;

    private FormGenerationBatch batch(UUID periodId, boolean dryRun, FormGenerationBatchStatus status,
                                       LocalDateTime generatedAt) {
        return FormGenerationBatch.reconstruct(UUID.randomUUID(), periodId, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, dryRun, status,
                dryRun ? 0 : 1, 0, 0, 0, generatedAt, generatedAt, "scheduler:period-opening", List.of());
    }

    @Test
    void handleReturnsThreeSummariesWhenOnlyTwoOfThreeHaveBatches() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();
        when(batchRepository.findByPeriodIds(anyCollectionOf3())).thenReturn(
                List.of(batch(p1, false, FormGenerationBatchStatus.COMPLETED, LocalDateTime.now()),
                        batch(p2, false, FormGenerationBatchStatus.COMPLETED, LocalDateTime.now())));

        ResponseEntity<List<FormGenerationSummaryDTO>> response = handler.handle(
                new GetPeriodGenerationSummariesQuery(List.of(p1.toString(), p2.toString(), p3.toString())));

        assertEquals(200, response.getStatusCode().value());
        List<FormGenerationSummaryDTO> body = response.getBody();
        assertEquals(3, body.size());
        assertTrue(body.stream().anyMatch(s -> s.getPeriodId().equals(p3.toString())
                && "NOT_GENERATED".equals(s.getStatus())));
    }

    @Test
    void handleCallsFindByPeriodIdsOnceWithAllThreeIdentifiers() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();
        when(batchRepository.findByPeriodIds(anyCollectionOf3())).thenReturn(List.of());

        handler.handle(new GetPeriodGenerationSummariesQuery(List.of(p1.toString(), p2.toString(), p3.toString())));

        ArgumentCaptor<Collection<UUID>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(batchRepository).findByPeriodIds(captor.capture());
        assertEquals(3, captor.getValue().size());
        assertTrue(captor.getValue().containsAll(List.of(p1, p2, p3)));
    }

    @Test
    void handleDedupesRepeatedIdentifierInInput() {
        UUID p1 = UUID.randomUUID();
        when(batchRepository.findByPeriodIds(anyCollectionOf3())).thenReturn(List.of());

        ResponseEntity<List<FormGenerationSummaryDTO>> response = handler.handle(
                new GetPeriodGenerationSummariesQuery(List.of(p1.toString(), p1.toString())));

        assertEquals(1, response.getBody().size());
    }

    @Test
    void handleReturnsEmptyListWith200WhenInputIsEmpty() {
        ResponseEntity<List<FormGenerationSummaryDTO>> response =
                handler.handle(new GetPeriodGenerationSummariesQuery(List.of()));

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void handleThrowsBadRequestWhenMoreThan100Identifiers() {
        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            tooMany.add(UUID.randomUUID().toString());
        }

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new GetPeriodGenerationSummariesQuery(tooMany)));

        assertEquals(400, exception.getBody().getStatus());
    }

    @Test
    void handleThrowsBadRequestNamingTheRejectedValueForNonUuidIdentifier() {
        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new GetPeriodGenerationSummariesQuery(List.of("nao-e-um-uuid"))));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("nao-e-um-uuid"));
    }

    @Test
    void handleUsesSameBatchSelectionCriteriaAsDetailHandlerWhenSeveralBatchesExist() {
        UUID p1 = UUID.randomUUID();
        FormGenerationBatch dryRunNewest = batch(p1, true, FormGenerationBatchStatus.DRY_RUN, LocalDateTime.now());
        FormGenerationBatch realOlder = batch(p1, false, FormGenerationBatchStatus.COMPLETED,
                LocalDateTime.now().minusDays(1));
        when(batchRepository.findByPeriodIds(anyCollectionOf3())).thenReturn(List.of(dryRunNewest, realOlder));

        ResponseEntity<List<FormGenerationSummaryDTO>> response = handler.handle(
                new GetPeriodGenerationSummariesQuery(List.of(p1.toString())));

        FormGenerationSummaryDTO summary = response.getBody().get(0);
        assertEquals(FormGenerationBatchStatus.COMPLETED.getCode(), summary.getStatus());
        assertEquals(false, summary.isDryRun());
    }

    @SuppressWarnings("unchecked")
    private static Collection<UUID> anyCollectionOf3() {
        return org.mockito.ArgumentMatchers.any();
    }
}
