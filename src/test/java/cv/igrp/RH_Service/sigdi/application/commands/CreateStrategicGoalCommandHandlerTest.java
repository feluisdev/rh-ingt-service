package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
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

    @Mock
    private StrategicGoalMapper goalMapper;

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
        when(goalMapper.toResponse(any(StrategicGoal.class))).thenAnswer(invocation -> {
            StrategicGoal g = invocation.getArgument(0);
            StategicGoalResponseDTO dto = new StategicGoalResponseDTO();
            dto.setYear(g.getYear());
            return dto;
        });

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
    void handleWithNullYearThrowsBadRequest() {
        CreateStategicGoalDTO dto = new CreateStategicGoalDTO();
        dto.setTitle("Objetivo Estratégico Teste");
        dto.setPerspective("FINANCIAL");
        dto.setDescription("Descrição de teste");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(null);

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> createStrategicGoalCommandHandler.handle(new CreateStrategicGoalCommand(dto)));

        assertEquals("O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.",
                ex.getBody().getTitle());
    }

    @Test
    void handleWithSuppliedYearButNoActivePeriodThrowsBadRequest() {
        // periodRepository is checked before identityRepository.findActive() in the handler,
        // so only the period stub is needed here -- stubbing findActive() would never be
        // exercised and would trip MockitoExtension's strict UnnecessaryStubbingException.
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2026, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.empty());

        CreateStategicGoalDTO dto = new CreateStategicGoalDTO();
        dto.setTitle("Objetivo Estratégico Teste");
        dto.setPerspective("FINANCIAL");
        dto.setDescription("Descrição de teste");
        dto.setWeight(BigDecimal.ONE);
        dto.setYear(2026);

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> createStrategicGoalCommandHandler.handle(new CreateStrategicGoalCommand(dto)));

        assertEquals("Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC",
                ex.getBody().getTitle());
    }
}
