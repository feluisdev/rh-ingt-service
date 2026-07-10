package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CreatePaaSubmissionPeriodCommandHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository repository;

    @InjectMocks
    private CreatePaaSubmissionPeriodCommandHandler handler;

    @Test
    void createSiadapIndividualPeriodSucceedsEvenWhenNoClosedPaaUnitLevelPeriodExists() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), start, end, 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(Purpose.SIADAP.getCode(), response.getBody().getPurpose());

        // Pitfall-1 regression guard: the PAA org->individual cascade must never be consulted for SIADAP
        verify(repository, never()).findByTypeAndYearAndStatusAndPurpose(
                eq(PaaLevel.UNIT_LEVEL), any(), eq("CLOSED"), eq(Purpose.PAA));
    }

    @Test
    void createPaaIndividualPeriodStillRequiresClosedPaaUnitLevelPeriod() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), start, end, 2026, Purpose.PAA.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "CLOSED", Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
    }

    @Test
    void createPaaUnitLevelPeriodWithNoPurposeInDtoDefaultsToPaa() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, null);

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(Purpose.PAA.getCode(), response.getBody().getPurpose());
    }
}
