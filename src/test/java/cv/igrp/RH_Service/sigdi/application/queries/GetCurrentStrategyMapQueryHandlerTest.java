package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapDataDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class GetCurrentStrategyMapQueryHandlerTest {

  @Mock
  private InstitutionalIdentityRepository identityRepository;

  @Mock
  private StrategicGoalRepository goalRepository;

  @Mock
  private StrategyMapLinkRepository linkRepository;

  @Mock
  private BscPerspectiveConfigRepository bscPerspectiveConfigRepository;

  private GetCurrentStrategyMapQueryHandler getCurrentStrategyMapQueryHandler;

  // Real mapper (not a Mockito mock), backed by the same mocked repository, so this test
  // exercises the actual delegated StrategicGoalMapper.toResponse(goal, labelsByCode) mapping
  // -- not a stubbed stand-in -- confirming the WR-01 delegation is wired correctly end to end
  // (74-REVIEW.md WR-01: GetCurrentStrategyMapQueryHandler must not hand-roll its own mapping).
  @BeforeEach
  void setUp() {
    StrategicGoalMapper goalMapper = new StrategicGoalMapper(bscPerspectiveConfigRepository);
    getCurrentStrategyMapQueryHandler = new GetCurrentStrategyMapQueryHandler(
        identityRepository, goalRepository, linkRepository, bscPerspectiveConfigRepository, goalMapper);
  }

  private InstitutionalIdentity activeIdentity() {
    return InstitutionalIdentity.create(UUID.randomUUID(), 2026, "Missão de teste",
        "Visão de teste", InstitutionalValues.of(List.of("Integridade")), "comentário de teste");
  }

  @Test
  void handleSourcesPerspectiveDescFromConfigLabelNotEnumDescription() {
    InstitutionalIdentity identity = activeIdentity();

    StrategicGoal goal = StrategicGoal.reconstruct(StrategicGoalId.gerarNovo(), UUID.randomUUID(),
        identity.getId(), "Objetivo Estratégico Teste", StrategicGoalsPerspective.FINANCIAL,
        BigDecimal.ONE, Estado.A, "Descrição de teste", 0.0, 0.0, 2026, Collections.emptyList());

    when(identityRepository.findActive()).thenReturn(Optional.of(identity));
    when(goalRepository.findByIdentityId(any())).thenReturn(List.of(goal));
    when(linkRepository.findByIdentityId(any())).thenReturn(List.of());
    when(bscPerspectiveConfigRepository.findAll())
        .thenReturn(List.of(BscPerspectiveConfig.reconstruct(UUID.randomUUID(), "FINANCIAL", "Financeira", 1)));

    ResponseEntity<StrategyMapDataDTO> response =
        getCurrentStrategyMapQueryHandler.handle(new GetCurrentStrategyMapQuery());

    assertEquals("Financeira", response.getBody().getGoals().get(0).getPerspectiveDesc());
    // WR-01 (74-REVIEW.md): the hand-rolled mapping this handler used to do dropped `year`
    // entirely. Delegating to StrategicGoalMapper.toResponse(goal, labelsByCode) must carry it.
    assertEquals(2026, response.getBody().getGoals().get(0).getYear());
  }

}
