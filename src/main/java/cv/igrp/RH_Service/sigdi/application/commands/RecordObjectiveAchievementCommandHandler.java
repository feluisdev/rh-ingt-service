package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.dto.RecordObjectiveAchievementRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecordObjectiveAchievementCommandHandler
    implements CommandHandler<RecordObjectiveAchievementCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(RecordObjectiveAchievementCommand command) {
    RecordObjectiveAchievementRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(req.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // WR-01: only the avaliador desta avaliação pode registar a execução dos objetivos.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEvaluatorId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliador desta avaliação pode registar a execução dos objetivos");

    SiadapEvaluation updated = evaluation.recordObjectiveAchievement(
        req.getObjectiveCode(),
        req.getAchievedValue(),
        req.getScore()
    );
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
