package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CreateTacticalActivityCommandHandlerTest {

    @Mock
    private EconomicClassifierPort economicClassifierPort;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @Mock
    private ActivityApprovalHistoryRecorder historyRecorder;

    @Mock
    private PaaActivityWindowPolicy windowPolicy;

    @InjectMocks
    private CreateTacticalActivityCommandHandler createTacticalActivityCommandHandler;

    @Test
    void handleWithActivePaaPeriodSucceeds() {
        UUID strategicGoalId = UUID.randomUUID();
        UUID organicUnitId = UUID.randomUUID();

        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(mock(StrategicGoal.class)));
        when(organicaLookupPort.findById(organicUnitId))
                .thenReturn(Optional.of(mock(OrganicaDTO.class)));
        // windowPolicy.requireOpenFor(...) is void and does nothing on a mock by default --
        // 136-11: os handlers deixaram de consultar o repositório de períodos diretamente,
        // consultam PaaActivityWindowPolicy.requireOpenFor.
        when(activityRepository.save(any(TacticalActivity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(strategicGoalId);
        dto.setOrganicUnitId(organicUnitId);
        dto.setTitle("Atividade de teste válida");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(10));
        dto.setPaaLevel(PaaLevel.UNIT_LEVEL.getCode());

        CreateTacticalActivityCommand command = new CreateTacticalActivityCommand(dto);

        ResponseEntity<TacticalActivityResponseDTO> response = createTacticalActivityCommandHandler.handle(command);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        verify(activityRepository, times(1)).save(any(TacticalActivity.class));

        // A-135-2AB (Phase 136, plano 136-10): a escrita de histórico da criação passou a viver
        // no colaborador único, com fromStatus = "NEW" como antes.
        verify(historyRecorder, times(1)).record(any(), eq("NEW"), any(), any(), any());

        // 136-11 (D-27): o handler já não consulta o repositório de períodos em linha --
        // pergunta à política, com o paaLevel resolvido do pedido (UNIT_LEVEL, por omissão).
        verify(windowPolicy, times(1)).requireOpenFor(PaaLevel.UNIT_LEVEL);
    }

    // 136-11: prova que a recusa da política chega até ao chamador e que nada é gravado --
    // o portão único (PaaActivityWindowPolicy) é mesmo consultado antes do save().
    @Test
    void handleRefusesAndSavesNothingWhenWindowPolicyRefuses() {
        UUID strategicGoalId = UUID.randomUUID();
        UUID organicUnitId = UUID.randomUUID();

        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(mock(StrategicGoal.class)));
        when(organicaLookupPort.findById(organicUnitId))
                .thenReturn(Optional.of(mock(OrganicaDTO.class)));
        doThrow(IgrpResponseStatusException.badRequest(
                        "Prazo não configurado para a submissão de atividades do PAA"))
                .when(windowPolicy).requireOpenFor(PaaLevel.UNIT_LEVEL);

        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(strategicGoalId);
        dto.setOrganicUnitId(organicUnitId);
        dto.setTitle("Atividade sem janela aberta");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(10));
        dto.setPaaLevel(PaaLevel.UNIT_LEVEL.getCode());

        CreateTacticalActivityCommand command = new CreateTacticalActivityCommand(dto);

        assertThrows(IgrpResponseStatusException.class,
                () -> createTacticalActivityCommandHandler.handle(command));

        verify(activityRepository, never()).save(any());
        verify(historyRecorder, never()).record(any(), any(), any(), any(), any());
    }
}
