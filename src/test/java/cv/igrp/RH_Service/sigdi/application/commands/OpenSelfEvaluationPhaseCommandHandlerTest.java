package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapSelfEvaluationOpenerSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.service.SelfEvaluationWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;

import java.math.BigDecimal;
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

/**
 * Mould: {@link FinalizeEvaluationCommandHandlerTest}. Six cases prove actor-before-window
 * ordering, the domain's own IN_PROGRESS guard is not duplicated here, and that a refused
 * request never reaches {@code save}.
 */
@ExtendWith(MockitoExtension.class)
class OpenSelfEvaluationPhaseCommandHandlerTest {

  private static final Integer YEAR = 2026;

  @Mock
  private SiadapEvaluationRepository evaluationRepository;

  @Mock
  private SiadapEvaluationMapper mapper;

  @Mock
  private CurrentEmployeeResolver currentEmployeeResolver;

  @Mock
  private SiadapSelfEvaluationOpenerSecurityProperties openerProperties;

  @Mock
  private SelfEvaluationWindowPolicy windowPolicy;

  @InjectMocks
  private OpenSelfEvaluationPhaseCommandHandler handler;

  private SiadapEvaluation buildEvaluation(EvaluationPhase phase) {
    return SiadapEvaluation.reconstruct(
        SiadapEvaluationId.gerarNovo(),
        UUID.randomUUID().toString(),
        YEAR,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        List.of(),
        List.of(),
        new BigDecimal("60"), new BigDecimal("40"),
        null, null, null, false,
        phase, AcceptanceStatus.ACCEPTED, null, false);
  }

  @Test
  void happyPath_actorAllowed_windowOpen_evaluationInProgress_transitionsToSelfEvaluation() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
    when(openerProperties.isSelfEvaluationOpener(openerId)).thenReturn(true);
    when(evaluationRepository.save(any(SiadapEvaluation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    ResponseEntity<SiadapEvaluationDTO> response = handler.handle(command);

    assertEquals(200, response.getStatusCode().value());
    ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
    verify(evaluationRepository, times(1)).save(captor.capture());
    assertEquals(EvaluationPhase.SELF_EVALUATION, captor.getValue().getPhase());
  }

  @Test
  void actorRefused_throwsForbidden_andNeverSaves() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String rejectedId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(rejectedId));
    when(openerProperties.isSelfEvaluationOpener(rejectedId)).thenReturn(false);

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    IgrpResponseStatusException exception =
        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

    assertEquals(403, exception.getBody().getStatus());
    verify(evaluationRepository, never()).save(any());
  }

  @Test
  void windowClosed_throwsFromPolicy_andNeverSaves() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
    when(openerProperties.isSelfEvaluationOpener(openerId)).thenReturn(true);
    org.mockito.Mockito.doThrow(IgrpResponseStatusException.badRequest(
            "Não existe uma janela de autoavaliação SIADAP ativa para o ano " + YEAR))
        .when(windowPolicy).requireOpenFor(YEAR);

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    verify(evaluationRepository, never()).save(any());
  }

  @Test
  void wrongPhase_domainRejects_withDomainMessage_andNeverSaves() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.SELF_EVALUATION);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
    when(openerProperties.isSelfEvaluationOpener(openerId)).thenReturn(true);

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    IgrpResponseStatusException exception =
        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

    assertEquals(400, exception.getBody().getStatus());
    assertEquals("Só é possível abrir autoavaliação em ciclos IN_PROGRESS",
        exception.getBody().getTitle());
    verify(evaluationRepository, never()).save(any());
  }

  @Test
  void evaluationNotFound_throwsNotFound_andActorIsNeverResolved() {
    when(evaluationRepository.findById(any())).thenReturn(Optional.empty());

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(UUID.randomUUID().toString());

    IgrpResponseStatusException exception =
        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

    assertEquals(404, exception.getBody().getStatus());
    verify(currentEmployeeResolver, never()).resolve();
  }

  @Test
  void actorRefused_withWindowAlsoClosed_errorIsForbiddenNotBadRequest_andWindowNeverChecked() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String rejectedId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(rejectedId));
    when(openerProperties.isSelfEvaluationOpener(rejectedId)).thenReturn(false);

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    IgrpResponseStatusException exception =
        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

    assertEquals(403, exception.getBody().getStatus());
    verify(windowPolicy, never()).requireOpenFor(any());
  }
}
