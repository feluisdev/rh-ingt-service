package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.CreateScenarioRequestDTO;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationResultRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the ERRO-03 fix: {@link CreateScenarioCommandHandler} must read {@code targetId}
 * (not the literal {@code scope} string) as the unit/goal UUID, and must roll back the
 * initial PROCESSING row on failure via {@code @Transactional}.
 */
@ExtendWith(MockitoExtension.class)
public class CreateScenarioCommandHandlerTest {

  @Mock
  private SimulationScenarioRepository scenarioRepository;

  @Mock
  private SimulationResultRepository resultRepository;

  @Mock
  private TacticalActivitiesEntityRepository activitiesRepository;

  @InjectMocks
  private CreateScenarioCommandHandler createScenarioCommandHandler;

  private CreateScenarioRequestDTO buildRequest(String scope, String targetId) {
    CreateScenarioRequestDTO req = new CreateScenarioRequestDTO();
    req.setName("Cenario de Teste");
    req.setType("BUDGET_CUT");
    req.setPercentage(new BigDecimal("10"));
    req.setScope(scope);
    req.setFiscalYear(2026);
    req.setTargetId(targetId);
    return req;
  }

  @Test
  void departmentIdScopeWithValidUuidTargetIdQueriesByThatUuidNotByScope() {
    UUID targetUuid = UUID.randomUUID();
    CreateScenarioRequestDTO req = buildRequest("UNIT_SPECIFIC", targetUuid.toString());
    CreateScenarioCommand command = new CreateScenarioCommand(req);

    when(scenarioRepository.save(any(SimulationScenario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(activitiesRepository.findAllByFiscalYearAndOrganicUnitId(eq(2026), any(UUID.class)))
        .thenReturn(List.of());

    ResponseEntity<?> response = createScenarioCommandHandler.handle(command);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(activitiesRepository).findAllByFiscalYearAndOrganicUnitId(eq(2026), uuidCaptor.capture());
    assertEquals(targetUuid, uuidCaptor.getValue(),
        "must query by the targetId UUID, not by the literal scope string");
    verify(activitiesRepository, never()).findAllByFiscalYear(anyInt());
  }

  @Test
  void departmentIdScopeWithNonUuidTargetIdThrowsStructuredBadRequest() {
    CreateScenarioRequestDTO req = buildRequest("UNIT_SPECIFIC", "not-a-uuid");
    CreateScenarioCommand command = new CreateScenarioCommand(req);

    when(scenarioRepository.save(any(SimulationScenario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
        () -> createScenarioCommandHandler.handle(command));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode(),
        "an invalid targetId must yield a structured 400, never an unhandled 500");
  }

  @Test
  void handleIsTransactionalSoAFailureDoesNotLeaveAnOrphanProcessingRow() throws NoSuchMethodException {
    // Unit tests cannot exercise real Spring transaction rollback, so assert the structural
    // guarantee instead: the annotation that makes rollback possible is present and correctly
    // configured (no readOnly, since this method both reads and writes).
    Method handleMethod = CreateScenarioCommandHandler.class.getMethod("handle", CreateScenarioCommand.class);
    Transactional transactional = handleMethod.getAnnotation(Transactional.class);
    assertNotNull(transactional,
        "handle() must be @Transactional so a mid-method failure rolls back the PROCESSING save");
    assertFalse(transactional.readOnly(),
        "handle() both reads and writes; readOnly=true would silently break the save");

    CreateScenarioRequestDTO req = buildRequest("UNIT_SPECIFIC", "still-not-a-uuid");
    CreateScenarioCommand command = new CreateScenarioCommand(req);
    when(scenarioRepository.save(any(SimulationScenario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertThrows(IgrpResponseStatusException.class, () -> createScenarioCommandHandler.handle(command));
    // The PROCESSING save did happen before the failure -- proving @Transactional (not a
    // try/catch swallow) is the mechanism relied upon to avoid the orphan row.
    verify(scenarioRepository, times(1)).save(any(SimulationScenario.class));
  }

  @Test
  void globalScopeQueriesAllActivitiesForFiscalYearAndIgnoresTargetId() {
    CreateScenarioRequestDTO req = buildRequest("GLOBAL", "not-a-uuid-and-should-be-ignored");
    CreateScenarioCommand command = new CreateScenarioCommand(req);

    when(scenarioRepository.save(any(SimulationScenario.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(activitiesRepository.findAllByFiscalYear(2026)).thenReturn(List.of());

    ResponseEntity<?> response = createScenarioCommandHandler.handle(command);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(activitiesRepository).findAllByFiscalYear(2026);
    verify(activitiesRepository, never()).findAllByFiscalYearAndOrganicUnitId(anyInt(), any(UUID.class));
  }
}
