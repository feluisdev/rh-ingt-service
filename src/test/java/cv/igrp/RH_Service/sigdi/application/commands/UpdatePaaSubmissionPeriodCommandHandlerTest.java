package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaSubmissionPeriodSequenceRules;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * 133-03 / JAN-03: the proof that PUT tactical/periods/{id} actually GRAVES the alteration, not
 * merely that the route responds. Case 1 below asserts on the object CAPTURED at repository.save
 * -- the status code alone was already shown (2026-09-05, activities PUT) to prove nothing about
 * whether a write reached the repository.
 */
@ExtendWith(MockitoExtension.class)
class UpdatePaaSubmissionPeriodCommandHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository repository;

    // @Spy (not a plain field) so Mockito's @InjectMocks constructor-injection strategy sees it
    // as a candidate and wires the real implementation in -- a plain uninitialized field would
    // otherwise be passed as null, since UpdatePaaSubmissionPeriodCommandHandler takes it as a
    // constructor parameter.
    @Spy
    private PaaSubmissionPeriodSequenceRules sequenceRules = new PaaSubmissionPeriodSequenceRules();

    @InjectMocks
    private UpdatePaaSubmissionPeriodCommandHandler handler;

    // Case 1 of 5: startDate in the future -> save is called, and the CAPTURED object carries the
    // new dates/year while id, purpose, type and status stay equal to the original. This assert,
    // not the status code, is what proves the alteration actually grave.
    @Test
    void alteringAFutureWindowSavesTheCapturedObjectWithNewScheduleAndUnchangedIdentity() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        UUID id = UUID.randomUUID();
        LocalDate originalStart = today.plusDays(10);
        LocalDate originalEnd = today.plusDays(20);

        PaaSubmissionPeriod period = PaaSubmissionPeriod.reconstruct(
                id, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, originalStart, originalEnd, "OPEN", 2030);

        LocalDate newStart = originalStart.plusDays(5);
        LocalDate newEnd = originalEnd.plusDays(5);
        UpdatePaaSubmissionPeriodDTO dto = new UpdatePaaSubmissionPeriodDTO(newStart, newEnd, 2030);

        when(repository.findById(id)).thenReturn(Optional.of(period));
        // Year unchanged (2030) -- Rule 3 always runs; the period's own id is excluded from the
        // candidate set, so returning only itself must not trigger a self-overlap.
        when(repository.findAllByYear(2030)).thenReturn(List.of(period));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new UpdatePaaSubmissionPeriodCommand(id, dto));

        assertEquals(200, response.getStatusCode().value());

        ArgumentCaptor<PaaSubmissionPeriod> captor = ArgumentCaptor.forClass(PaaSubmissionPeriod.class);
        verify(repository).save(captor.capture());
        PaaSubmissionPeriod saved = captor.getValue();

        assertEquals(newStart, saved.getStartDate());
        assertEquals(newEnd, saved.getEndDate());
        assertEquals(2030, saved.getYear());
        assertEquals(id, saved.getId());
        assertEquals(Purpose.SIADAP, saved.getPurpose());
        assertEquals(PaaLevel.INDIVIDUAL_LEVEL, saved.getType());
        assertEquals("OPEN", saved.getStatus());
    }

    // Case 2 of 5: a CLOSED window has no correction path (D-18/T-140) -> 422, save never called.
    @Test
    void refusesToAlterAClosedWindowAndNeverCallsSave() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        UUID id = UUID.randomUUID();
        PaaSubmissionPeriod closedPeriod = PaaSubmissionPeriod.reconstruct(
                id, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.plusDays(10), today.plusDays(20), "CLOSED", 2030);

        UpdatePaaSubmissionPeriodDTO dto = new UpdatePaaSubmissionPeriodDTO(
                today.plusDays(15), today.plusDays(25), 2030);

        when(repository.findById(id)).thenReturn(Optional.of(closedPeriod));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdatePaaSubmissionPeriodCommand(id, dto)));

        assertEquals(422, exception.getBody().getStatus());
        verify(repository, never()).save(any());
    }

    // Case 3 of 5: the window's CURRENT startDate has already passed -> 422, save never called.
    // This is the guard that makes the alteration action disappear the moment the window opens.
    @Test
    void refusesToAlterAWindowWhoseCurrentStartDateHasAlreadyPassed() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        UUID id = UUID.randomUUID();
        PaaSubmissionPeriod startedPeriod = PaaSubmissionPeriod.reconstruct(
                id, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.minusDays(5), today.plusDays(20), "OPEN", 2030);

        UpdatePaaSubmissionPeriodDTO dto = new UpdatePaaSubmissionPeriodDTO(
                today.plusDays(1), today.plusDays(25), 2030);

        when(repository.findById(id)).thenReturn(Optional.of(startedPeriod));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdatePaaSubmissionPeriodCommand(id, dto)));

        assertEquals(422, exception.getBody().getStatus());
        verify(repository, never()).save(any());
    }

    // Case 4 of 5: the NEW startDate is in the past -- moving it there would be reopening through
    // the back door, which T-140 refuses -> 422, save never called.
    @Test
    void refusesWhenTheNewStartDateIsInThePast() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        UUID id = UUID.randomUUID();
        PaaSubmissionPeriod period = PaaSubmissionPeriod.reconstruct(
                id, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.plusDays(10), today.plusDays(20), "OPEN", 2030);

        UpdatePaaSubmissionPeriodDTO dto = new UpdatePaaSubmissionPeriodDTO(
                today.minusDays(1), today.plusDays(25), 2030);

        when(repository.findById(id)).thenReturn(Optional.of(period));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdatePaaSubmissionPeriodCommand(id, dto)));

        assertEquals(422, exception.getBody().getStatus());
        verify(repository, never()).save(any());
    }

    // Case 5 of 5: Rule 3 is reused, not reimplemented -- an alteration that would overlap the
    // nearest configured neighbor is refused with the same "Sobrepõe-se a ..." title creation
    // uses, and save is never called.
    @Test
    void refusesWhenTheNewScheduleOverlapsTheNearestConfiguredNeighbor() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        UUID id = UUID.randomUUID();
        // SIADAP_INTERIM is position 4; startDate/endDate anchored far in the future so this test
        // is not sensitive to which year "today" falls in.
        PaaSubmissionPeriod period = PaaSubmissionPeriod.reconstruct(
                id, Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2030, 6, 1), LocalDate.of(2030, 6, 30), "OPEN", 2030);
        // SIADAP is position 3, the immediately preceding position.
        PaaSubmissionPeriod neighbor = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2030, 1, 1), LocalDate.of(2030, 6, 15), "CLOSED", 2030);

        UpdatePaaSubmissionPeriodDTO dto = new UpdatePaaSubmissionPeriodDTO(
                LocalDate.of(2030, 6, 10), LocalDate.of(2030, 7, 10), 2030);

        when(repository.findById(id)).thenReturn(Optional.of(period));
        when(repository.findAllByYear(2030)).thenReturn(List.of(period, neighbor));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new UpdatePaaSubmissionPeriodCommand(id, dto)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Sobrepõe-se"));
        assertTrue(exception.getBody().getTitle().contains("Avaliação de Desempenho (SIADAP)"));
        verify(repository, never()).save(any());
    }
}
