package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
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
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Mould: {@link FinalizeEvaluationCommandHandlerTest}. Proves that business rule ordering
 * (window before the domain's own IN_PROGRESS guard) still holds, and that a refused request
 * never reaches {@code save}.
 *
 * <p>Phase 115/AUT-04: authorization is no longer this handler's concern -- the two cases that
 * asserted a 403 for a caller outside the configured opener list were removed. Equivalent
 * coverage lives in {@code ComplianceControllerMethodSecurityTest#openSelfEvaluationPhase_deniesWithoutPermission}
 * and {@code #openSelfEvaluationPhase_allowsMatchingPermission}, which prove the
 * {@code @PreAuthorize} guard on {@code ComplianceController#openSelfEvaluationPhase}. What
 * this class still owns, and is the reason {@link CurrentEmployeeResolver} remains a mock here,
 * is the repudiation-mitigation log line: {@link #logsAuthorOnSuccessfulOpen_currentEmployeeIdIsInTheLogLine()}
 * exists precisely so a future cleanup that mistakes the resolver for a leftover of this
 * migration fails loudly instead of silently dropping the audit trail.
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

  private static ListAppender<ILoggingEvent> attachAppender() {
    Logger logger =
        (Logger) LoggerFactory.getLogger(OpenSelfEvaluationPhaseCommandHandler.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    return appender;
  }

  private static void detachAppender(ListAppender<ILoggingEvent> appender) {
    Logger logger =
        (Logger) LoggerFactory.getLogger(OpenSelfEvaluationPhaseCommandHandler.class);
    logger.detachAppender(appender);
    appender.stop();
  }

  @Test
  void happyPath_windowOpen_evaluationInProgress_transitionsToSelfEvaluation() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
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
  void logsAuthorOnSuccessfulOpen_currentEmployeeIdIsInTheLogLine() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
    when(evaluationRepository.save(any(SiadapEvaluation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(mapper.toFullDto(any(SiadapEvaluation.class))).thenReturn(new SiadapEvaluationDTO());

    OpenSelfEvaluationPhaseCommand command =
        new OpenSelfEvaluationPhaseCommand(evaluation.getId().getStringValor());

    ListAppender<ILoggingEvent> appender = attachAppender();
    try {
      handler.handle(command);

      boolean loggedAuthor =
          appender.list.stream()
              .anyMatch(
                  event ->
                      event.getLevel() == Level.INFO
                          && event.getFormattedMessage().contains(openerId));
      assertTrue(loggedAuthor);
      verify(currentEmployeeResolver, times(1)).resolve();
    } finally {
      detachAppender(appender);
    }
  }

  @Test
  void windowClosed_throwsFromPolicy_andNeverSaves() {
    SiadapEvaluation evaluation = buildEvaluation(EvaluationPhase.IN_PROGRESS);
    String openerId = UUID.randomUUID().toString();

    when(evaluationRepository.findById(any())).thenReturn(Optional.of(evaluation));
    when(currentEmployeeResolver.resolve()).thenReturn(FuncionarioId.from(openerId));
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
}
