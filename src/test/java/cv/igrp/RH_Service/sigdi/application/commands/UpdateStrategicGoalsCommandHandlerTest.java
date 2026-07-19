package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
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
}
