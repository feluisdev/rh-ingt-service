package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TaticalActivityHistoryEntityRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// A-135-2AB (Phase 136, plano 136-10): este colaborador é o dono único da escrita de
// t_activity_approval_history -- antes dele, dois handlers construíam a entidade à mão, com o
// mesmo bloco de dez linhas duplicado, e cinco outros não escreviam histórico nenhum.
@ExtendWith(MockitoExtension.class)
class ActivityApprovalHistoryRecorderTest {

  @Mock
  private TaticalActivityHistoryEntityRepository historyRepository;

  @Mock
  private TacticalActivitiesEntityRepository entityRepository;

  @Mock
  private SecurityContextHelper securityContextHelper;

  @InjectMocks
  private ActivityApprovalHistoryRecorder recorder;

  private TacticalActivitiesEntity activityEntity(UUID id, UUID institutionId) {
    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setId(id);
    entity.setInstitutionId(institutionId);
    return entity;
  }

  // Grava uma linha com os campos certos, incluindo o actorId resolvido a partir do contexto
  // de segurança.
  @Test
  void recordWritesOneRowWithTheGivenFields() {
    UUID rawId = UUID.randomUUID();
    UUID institutionId = UUID.randomUUID();
    UUID actorId = UUID.randomUUID();
    TacticalActivitiesEntity entity = activityEntity(rawId, institutionId);

    when(entityRepository.findById(rawId)).thenReturn(Optional.of(entity));
    when(securityContextHelper.getCurrentUserId()).thenReturn(actorId.toString());

    recorder.record(TacticalActivityId.from(rawId), "PENDING_TACTICAL", "PENDING_STRATEGIC",
        "PENDING_STRATEGIC", "Aprovado, prossiga");

    ArgumentCaptor<TaticalActivityHistoryEntity> captor =
        ArgumentCaptor.forClass(TaticalActivityHistoryEntity.class);
    verify(historyRepository).save(captor.capture());

    TaticalActivityHistoryEntity saved = captor.getValue();
    assertEquals(institutionId, saved.getInstitutionId());
    assertEquals(entity, saved.getActivityId());
    assertEquals("PENDING_TACTICAL", saved.getFromStatus());
    assertEquals("PENDING_STRATEGIC", saved.getToStatus());
    assertEquals("PENDING_STRATEGIC", saved.getAction());
    assertEquals("Aprovado, prossiga", saved.getComment());
    assertEquals(actorId, saved.getActorId());
  }

  // Com a entidade ausente, não grava -- mas, ao contrário dos dois blocos que substitui, o
  // silêncio deixou de ser total: fica um warn (verificado aqui apenas pela ausência de save,
  // já que capturar a saída do logger exigiria um appender dedicado fora do âmbito deste teste).
  @Test
  void recordWritesNothingWhenActivityIsNotFound() {
    UUID rawId = UUID.randomUUID();
    when(entityRepository.findById(rawId)).thenReturn(Optional.empty());

    recorder.record(TacticalActivityId.from(rawId), "NEW", "PENDING_TACTICAL", "PENDING_TACTICAL", null);

    verify(historyRepository, never()).save(any());
  }

  // O actorId fica null quando o contexto de segurança não resolve a um UUID -- é o que
  // acontece hoje em todos os ambientes deste projeto, porque SECURITY_ENABLED=false
  // (A-132-121): getCurrentUserId() devolve "system", que não é um UUID válido.
  @Test
  void recordLeavesActorIdNullWhenSecurityContextDoesNotResolve() {
    UUID rawId = UUID.randomUUID();
    UUID institutionId = UUID.randomUUID();
    TacticalActivitiesEntity entity = activityEntity(rawId, institutionId);

    when(entityRepository.findById(rawId)).thenReturn(Optional.of(entity));
    when(securityContextHelper.getCurrentUserId()).thenReturn("system");

    recorder.record(TacticalActivityId.from(rawId), "NEW", "PENDING_TACTICAL", "PENDING_TACTICAL", null);

    ArgumentCaptor<TaticalActivityHistoryEntity> captor =
        ArgumentCaptor.forClass(TaticalActivityHistoryEntity.class);
    verify(historyRepository).save(captor.capture());

    assertNull(captor.getValue().getActorId());
  }
}
