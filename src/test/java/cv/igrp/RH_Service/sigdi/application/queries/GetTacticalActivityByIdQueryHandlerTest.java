package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityDetailDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// A-136-51 (Phase 136, plano 136-15): GetTacticalActivityByIdQueryHandler nunca populava
// paaLevel/paaLevelDesc na resposta do GET de detalhe -- o ecrã de edição
// (tactical/paa/edit/[id]/page.tsx:77) caía sempre no fallback "UNIT_LEVEL", independente do
// nível real da atividade. Não existia teste nenhum para este handler antes deste plano.
@ExtendWith(MockitoExtension.class)
class GetTacticalActivityByIdQueryHandlerTest {

  @Mock
  private TacticalActivitiesEntityRepository repository;

  @Mock
  private OrganicaLookupPort organicaLookupPort;

  @Mock
  private FuncionarioLookupPort funcionarioLookupPort;

  @InjectMocks
  private GetTacticalActivityByIdQueryHandler handler;

  private TacticalActivitiesEntity entityWithLevel(String paaLevel) {
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setId(UUID.randomUUID());
    entity.setStrategicGoalId(UUID.randomUUID());
    entity.setTitle("Atividade de teste");
    entity.setStatus("DRAFT");
    entity.setPaaLevel(paaLevel);
    entity.setVersion(0);
    return entity;
  }

  @Test
  void handleReturnsPaaLevelAndDescForUnitLevelActivity() {
    TacticalActivitiesEntity entity = entityWithLevel(PaaLevel.UNIT_LEVEL.getCode());
    when(repository.findByIdOrThrow(any(UUID.class))).thenReturn(entity);

    GetTacticalActivityByIdQuery query = new GetTacticalActivityByIdQuery();
    query.setId(entity.getId().toString());

    ResponseEntity<TacticalActivityDetailDTO> response = handler.handle(query);

    assertEquals(PaaLevel.UNIT_LEVEL.getCode(), response.getBody().getPaaLevel());
    assertEquals(PaaLevel.UNIT_LEVEL.getDescription(), response.getBody().getPaaLevelDesc());
  }

  @Test
  void handleReturnsPaaLevelAndDescForIndividualLevelActivity() {
    TacticalActivitiesEntity entity = entityWithLevel(PaaLevel.INDIVIDUAL_LEVEL.getCode());
    when(repository.findByIdOrThrow(any(UUID.class))).thenReturn(entity);

    GetTacticalActivityByIdQuery query = new GetTacticalActivityByIdQuery();
    query.setId(entity.getId().toString());

    ResponseEntity<TacticalActivityDetailDTO> response = handler.handle(query);

    assertEquals(PaaLevel.INDIVIDUAL_LEVEL.getCode(), response.getBody().getPaaLevel());
    assertEquals(PaaLevel.INDIVIDUAL_LEVEL.getDescription(), response.getBody().getPaaLevelDesc());
  }

  // A entidade tem paaLevel com omissão "UNIT_LEVEL" (coluna paa_level,136-15/entidade), mas
  // uma leitura direta -- sem passar pelo construtor de fábrica -- pode deixá-lo nulo. Prova
  // que o handler não lança nesse caso e devolve paaLevelDesc nulo em vez de inventar um valor.
  @Test
  void handleLeavesPaaLevelDescNullWhenEntityHasNoPaaLevel() {
    TacticalActivitiesEntity entity = entityWithLevel(null);
    when(repository.findByIdOrThrow(any(UUID.class))).thenReturn(entity);

    GetTacticalActivityByIdQuery query = new GetTacticalActivityByIdQuery();
    query.setId(entity.getId().toString());

    ResponseEntity<TacticalActivityDetailDTO> response = handler.handle(query);

    assertNull(response.getBody().getPaaLevel());
    assertNull(response.getBody().getPaaLevelDesc());
  }
}
