package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UpdateTacticalActivityCommandHandlerTest {

    @Mock
    private EconomicClassifierPort economicClassifierPort;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private UpdateTacticalActivityCommandHandler handler;

    @Test
    void handleWithActivePaaPeriodSucceeds() {
        int currentYear = Year.now().getValue();

        UUID strategicGoalId = UUID.randomUUID();
        UUID organicUnitId = UUID.randomUUID();

        // Existing entity's level is UNIT_LEVEL -- the check must read this, not the request DTO.
        TacticalActivity existingActivity = TacticalActivity.create(
                UUID.randomUUID(),
                StrategicGoalId.from(strategicGoalId),
                organicUnitId,
                "Atividade existente válida",
                null,
                null,
                null,
                null,
                null,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
                null,
                PaaLevel.UNIT_LEVEL);

        when(activityRepository.findById(any(TacticalActivityId.class)))
                .thenReturn(Optional.of(existingActivity));
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(mock(StrategicGoal.class)));
        when(organicaLookupPort.findById(organicUnitId))
                .thenReturn(Optional.of(mock(OrganicaDTO.class)));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, currentYear, Purpose.PAA))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));
        when(activityRepository.save(any(TacticalActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(strategicGoalId);
        dto.setOrganicUnitId(organicUnitId);
        dto.setTitle("Atividade atualizada válida");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));

        UpdateTacticalActivityCommand command =
                new UpdateTacticalActivityCommand(dto, existingActivity.getId().getStringValor());

        ResponseEntity<TacticalActivityResponseDTO> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(activityRepository, times(1)).save(any(TacticalActivity.class));
    }

    // PAA-02: proves resistance to a direct call to the handler, bypassing the UI entirely --
    // the guard must live in the domain aggregate, not only be hidden in the interface.
    @Test
    void handleRejectsBudgetChangeWhenActivityIsApproved() {
        int currentYear = Year.now().getValue();

        UUID strategicGoalId = UUID.randomUUID();
        UUID organicUnitId = UUID.randomUUID();

        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");

        TacticalActivity draft = TacticalActivity.create(
                UUID.randomUUID(),
                StrategicGoalId.from(strategicGoalId),
                organicUnitId,
                "Atividade aprovada com orçamento",
                null,
                null,
                null,
                null,
                null,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
                originalBudget,
                PaaLevel.UNIT_LEVEL);
        TacticalActivity existingActivity = draft.submit().approve().approve();

        when(activityRepository.findById(any(TacticalActivityId.class)))
                .thenReturn(Optional.of(existingActivity));
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(mock(StrategicGoal.class)));
        when(organicaLookupPort.findById(organicUnitId))
                .thenReturn(Optional.of(mock(OrganicaDTO.class)));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, currentYear, Purpose.PAA))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));
        // Available budget deliberately generous so the exception comes from the PAA-02 guard,
        // not from the unrelated budget-limit check earlier in the handler.
        when(economicClassifierPort.getBudget("02.02.01"))
                .thenReturn(new BudgetInfoDTO("02.02.01", new BigDecimal("999999"), "CVE", LocalDate.now()));
        // lenient: this stub is only exercised in the pre-guard RED state (where update()
        // succeeds and the handler reaches save()); once the guard is in place, the guard
        // throws first and save() is never invoked, which would otherwise trip Mockito's
        // strict-stubs UnnecessaryStubbingException.
        lenient().when(activityRepository.save(any(TacticalActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(strategicGoalId);
        dto.setOrganicUnitId(organicUnitId);
        dto.setTitle("Atividade aprovada com orçamento alterado");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setBudgetEstimated(new BigDecimal("2000"));
        dto.setEconomicClassifier("02.02.01");

        UpdateTacticalActivityCommand command =
                new UpdateTacticalActivityCommand(dto, existingActivity.getId().getStringValor());

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        verify(activityRepository, never()).save(any());
    }
}
