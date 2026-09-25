package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.service.StrategicGoalWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CancelStrategicGoalCommandHandler
    implements CommandHandler<CancelStrategicGoalCommand, ResponseEntity<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CancelStrategicGoalCommandHandler.class);

  private final StrategicGoalRepository goalRepository;
  private final StrategicGoalWindowPolicy windowPolicy;

  public CancelStrategicGoalCommandHandler(StrategicGoalRepository goalRepository,
                                           StrategicGoalWindowPolicy windowPolicy) {
    this.goalRepository = goalRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  public ResponseEntity<String> handle(CancelStrategicGoalCommand command) {
    LOGGER.debug("CancelStrategicGoalCommand : {}", command);

    StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(command.getId()));

    StrategicGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Objetivo estratégico não encontrado"));

    if (!goal.isActive()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
          "Objetivo já se encontra inativo");
    }

    // FIX-09 / A-124-04 (Média): cancelling a strategic goal now requires an OPEN
    // PAA/BSC submission window, exactly like creating and updating one already did.
    //
    // 1. The finding this closes is A-124-04: the DELETE answered 204 with the period
    //    CLOSED, measured end to end through the screen in docs/qa/130-EVIDENCIA-LACUNA-124.md.
    // 2. The asymmetry it removes: CreateStrategicGoalCommandHandler and
    //    UpdateStrategicGoalsCommandHandler demanded an open window; this handler did not
    //    even receive the period repository. Cancelling is always possible and restoring is
    //    not -- there is no reactivation route -- so the unguarded side was the destructive one.
    // 3. Why IgrpResponseStatusException and not the plain ResponseStatusException used by the
    //    two guards above: so the RFC 7807 title survives to the BFF (BLOQ-07), which is the
    //    same reason written at UpdateStrategicGoalsCommandHandler. A plain
    //    ResponseStatusException collapses the message and the BFF route shows a generic error
    //    instead of the real reason. The two pre-existing guards are deliberately left as they
    //    are: converting them would change behaviour no requirement asks for.
    // 4. Why this guard comes AFTER the inactive-goal guard and never before: cancelling an
    //    already inactive goal is a bad request regardless of the deadline, and running the
    //    period check first would answer 400 (deadline) where the service answers 422 (state)
    //    today -- a behaviour change no requirement asks for.
    //
    // Mirrors UpdateStrategicGoalsCommandHandler literally, with the single difference that
    // there is no DTO here: the effective year can only be the goal's own year.
    Integer effectiveYear = goal.getYear();
    if (effectiveYear == null) {
      throw IgrpResponseStatusException.badRequest(
          "O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.");
    }
    // Fase 136-06: critério movido para StrategicGoalWindowPolicy -- deixa de ser consulta em
    // linha, para não repetir a primeira cópia do mesmo critério.
    windowPolicy.requireOpenFor(effectiveYear);

    StrategicGoal cancelled = goal.cancel();
    goalRepository.save(cancelled);

    return ResponseEntity.noContent().build();
  }
}
