package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategicIndicatorDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
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
public class UpdateStrategicGoalsCommandHandler implements CommandHandler<UpdateStrategicGoalsCommand, ResponseEntity<StategicGoalResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStrategicGoalsCommandHandler.class);

  private final StrategicGoalRepository goalRepository;
  private final StrategicGoalMapper goalMapper;
  private final PaaSubmissionPeriodRepository periodRepository;

  public UpdateStrategicGoalsCommandHandler(StrategicGoalRepository goalRepository,
                                            StrategicGoalMapper goalMapper,
                                            PaaSubmissionPeriodRepository periodRepository) {
    this.goalRepository = goalRepository;
    this.goalMapper = goalMapper;
    this.periodRepository = periodRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<StategicGoalResponseDTO> handle(UpdateStrategicGoalsCommand command) {
    LOGGER.debug("UpdateStrategicGoalsCommand : {}", command);

    StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(command.getId()));

    StrategicGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic goal not found"));

    var dto = command.getUpdatestategicgoal();

    // PRAZO-01/02/03: the EFFECTIVE year (supplied on the DTO, or the existing goal's year
    // when the edit omits it -- StrategicGoal.update() preserves the existing year in that
    // case, 72-RESEARCH.md Pitfall 2) is now mandatory on every update. This also catches a
    // legacy goal (year == null) edited without ever touching the year field. The deadline
    // check that follows is therefore always evaluated. Uses IgrpResponseStatusException
    // (not the plain ResponseStatusException used above for the not-found case) so the RFC
    // 7807 title survives to the BFF (BLOQ-07).
    Integer effectiveYear = dto.getYear() != null ? dto.getYear() : goal.getYear();
    if (effectiveYear == null) {
      throw IgrpResponseStatusException.badRequest(
          "O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.");
    }
    periodRepository.findActiveByTypeAndYearAndPurpose(
            PaaLevel.UNIT_LEVEL, effectiveYear, Purpose.PAA_BSC_OBJECTIVES)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC"));

    // FIX-01 / A-124-01: the indicator list is decided explicitly here, and the two cases are
    // NOT the same thing.
    //   - indicators == null (the "indicators" key is ABSENT from the request body) means
    //     "do not touch": the goal keeps the indicators it already has, and goal.update()
    //     receives exactly that list.
    //   - indicators != null, INCLUDING the empty list, means "replace": [] removes them all.
    //     That is deliberate and it is not a defect. There is no dedicated indicator endpoint
    //     in this service, so updating the goal is the only way the product offers to remove
    //     the last KPI; a guard that ignored [] would make that impossible by any route.
    //
    // This guard depends on UpdateStategicGoalDTO leaving the "indicators" field WITHOUT an
    // initializer. If a regeneration by iGRP Studio restores "= new ArrayList<>()", the
    // information is destroyed before it reaches this method and no code here can recover it:
    // every absent key would arrive as an empty list and edit-then-save would wipe the KPIs
    // again. The half of the fix that survives such a regeneration is therefore not this
    // "if" but UpdateStategicGoalDtoIndicatorsContractTest, which turns the regression from
    // silent data loss into a red build.
    java.util.List<cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator> domainIndicators = goal.getIndicators();
    if (dto.getIndicators() != null) {
        domainIndicators = dto.getIndicators().stream().map(indDto -> {
            if (indDto.getId() != null) {
                return cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator.reconstruct(
                    indDto.getId(),
                    indDto.getTitle(),
                    indDto.getFormula(),
                    indDto.getTarget(),
                    indDto.getEvaluationCriteria(),
                    indDto.getInfoSource(),
                    indDto.getWeight(),
                    indDto.getCriteriaSuperado(),
                    indDto.getCriteriaSeguranca(),
                    indDto.getCriteriaAlcancado(),
                    indDto.getCriteriaInsuficiente()
                );
            } else {
                return cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator.create(
                    indDto.getTitle(),
                    indDto.getFormula(),
                    indDto.getTarget(),
                    indDto.getEvaluationCriteria(),
                    indDto.getInfoSource(),
                    indDto.getWeight(),
                    indDto.getCriteriaSuperado(),
                    indDto.getCriteriaSeguranca(),
                    indDto.getCriteriaAlcancado(),
                    indDto.getCriteriaInsuficiente()
                );
            }
        }).collect(java.util.stream.Collectors.toList());
    }

    StrategicGoal updated = goal.update(dto.getTitle(), dto.getDescription(), dto.getWeight(), dto.getYear(), domainIndicators);
    StrategicGoal saved = goalRepository.save(updated);

    return ResponseEntity.ok(goalMapper.toResponse(saved));
  }
}