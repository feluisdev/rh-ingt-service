package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
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
}
