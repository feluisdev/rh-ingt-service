package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.CreateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.math.BigDecimal;
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
public class CreateStrategicGoalCommandHandlerTest {

    @Mock
    private InstitutionalIdentityRepository identityRepository;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private CreateStrategicGoalCommandHandler createStrategicGoalCommandHandler;

    private InstitutionalIdentity activeIdentity() {
        return InstitutionalIdentity.create(UUID.randomUUID(), 2026, "Missão de teste",
                "Visão de teste", InstitutionalValues.of(List.of("Integridade")), "comentário de teste");
    }

    @Test
    void handleWithSuppliedYearPersistsAndReturnsIt() {
        when(identityRepository.findActive()).thenReturn(Optional.of(activeIdentity()));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2026, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        CreateStategicGoalDTO dto = new CreateStategicGoalDTO();
        dto.setTitle("Objetivo Estratégico Teste");
        dto.setPerspective("FINANCIAL");
        dto.setDescription("Descrição de teste");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);

        ResponseEntity<StategicGoalResponseDTO> response =
                createStrategicGoalCommandHandler.handle(new CreateStrategicGoalCommand(dto));

        assertEquals(2026, response.getBody().getYear());
    }

    @Test
    void handleWithNullYearSucceedsAndReturnsNullYear() {
        when(identityRepository.findActive()).thenReturn(Optional.of(activeIdentity()));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStategicGoalDTO dto = new CreateStategicGoalDTO();
        dto.setTitle("Objetivo Estratégico Teste");
        dto.setPerspective("FINANCIAL");
        dto.setDescription("Descrição de teste");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null);

        ResponseEntity<StategicGoalResponseDTO> response =
                createStrategicGoalCommandHandler.handle(new CreateStrategicGoalCommand(dto));

        assertNull(response.getBody().getYear());
    }
}
