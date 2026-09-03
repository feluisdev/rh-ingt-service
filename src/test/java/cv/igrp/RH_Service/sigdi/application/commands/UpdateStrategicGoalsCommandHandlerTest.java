package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import java.math.BigDecimal;
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
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class UpdateStrategicGoalsCommandHandlerTest {

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private StrategicGoalMapper goalMapper;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private UpdateStrategicGoalsCommandHandler updateStrategicGoalsCommandHandler;

    @Test
    void handleChangesExistingGoalsYearToTheSuppliedValue() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2027);

        ResponseEntity<StategicGoalResponseDTO> response = updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(2027, savedCaptor.getValue().getYear());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void handleWithNullYearPreservesExistingGoalsYear() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2020, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null); // omitted on the edit form -- must NOT clear the existing year

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(2020, savedCaptor.getValue().getYear());
    }

    @Test
    void handleWithLegacyNullYearGoalAndOmittedYearThrowsBadRequest() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                null, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null); // legacy goal + edit that never touches year -- effective year stays null

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> updateStrategicGoalsCommandHandler.handle(
                        new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString())));

        assertEquals("O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.",
                ex.getBody().getTitle());
    }

    @Test
    void handleWithSuppliedYearButNoActivePeriodThrowsBadRequest() {
        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo Original",
                StrategicGoalsPerspective.FINANCIAL, BigDecimal.ONE, "Descrição original",
                2020, new ArrayList<>());

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2026, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.empty());

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo Original");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> updateStrategicGoalsCommandHandler.handle(
                        new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString())));

        assertEquals("Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC",
                ex.getBody().getTitle());
    }

    /**
     * FIX-01 / A-124-01, first half: an ABSENT "indicators" key must preserve what the goal
     * already has. Jackson leaves the DTO field null when the key is missing, and a null list
     * means "do not touch". Before this fix the DTO initialised the field to an empty list, the
     * handler could never see null, and every edit silently wiped the goal's KPIs.
     */
    @Test
    void handleWithAbsentIndicatorsPreservesTheExistingOnes() {
        StrategicIndicator existingIndicator = StrategicIndicator.create(
                "Técnicos certificados em cadastro predial", "certificados / inscritos",
                BigDecimal.valueOf(40), null, "Registo interno", BigDecimal.ONE,
                null, null, null, null);

        List<StrategicIndicator> existingIndicators = new ArrayList<>();
        existingIndicators.add(existingIndicator);

        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo com KPI",
                StrategicGoalsPerspective.LEARNING, BigDecimal.ONE, "Descrição original",
                2026, existingIndicators);

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2026, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo com KPI");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);
        // setIndicators is deliberately NOT called: this is the absent-key case.

        assertNull(dto.getIndicators(),
                "The DTO must leave indicators null when the key is absent, otherwise the handler "
                        + "cannot tell absent from empty and the KPIs are wiped (A-124-01)");

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());

        List<StrategicIndicator> savedIndicators = savedCaptor.getValue().getIndicators();
        assertEquals(1, savedIndicators.size());
        assertEquals("Técnicos certificados em cadastro predial", savedIndicators.get(0).getTitle());
        assertEquals(existingIndicator.getId(), savedIndicators.get(0).getId());
    }

    /**
     * FIX-01 / A-124-01, second half: an EXPLICIT empty list must replace the existing one, so the
     * goal ends up with zero indicators. This removal is INTENTIONAL and it is not a defect. There
     * is no dedicated indicator endpoint in this service, so updating the goal is the only route
     * the product offers to delete the last KPI; a guard that ignored [] would make that
     * impossible by any route, which would be new debt of our own making.
     */
    @Test
    void handleWithExplicitEmptyIndicatorsRemovesThemAllOnPurpose() {
        List<StrategicIndicator> existingIndicators = new ArrayList<>();
        existingIndicators.add(StrategicIndicator.create(
                "KPI a remover", "processos digitais / total", BigDecimal.valueOf(80),
                null, "Registo interno", BigDecimal.ONE, null, null, null, null));

        StrategicGoal existingGoal = StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo com KPI",
                StrategicGoalsPerspective.PROCESS, BigDecimal.ONE, "Descrição original",
                2026, existingIndicators);

        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.of(existingGoal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2026, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo com KPI");
        dto.setDescription("Descrição original");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);
        dto.setIndicators(new ArrayList<>()); // explicit [] -- the only way to drop the last KPI

        updateStrategicGoalsCommandHandler.handle(
                new UpdateStrategicGoalsCommand(dto, UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertEquals(0, savedCaptor.getValue().getIndicators().size());
    }
}
