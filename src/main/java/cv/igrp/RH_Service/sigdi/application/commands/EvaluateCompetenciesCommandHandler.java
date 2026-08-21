package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.dto.EvaluateCompetenciesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ACTOR-CHECK: ENFORCED -- evaluation.evaluatorId; only the avaliador may evaluate competencies
@Component
@RequiredArgsConstructor
public class EvaluateCompetenciesCommandHandler
    implements CommandHandler<EvaluateCompetenciesCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(EvaluateCompetenciesCommand command) {
    EvaluateCompetenciesRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(req.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // WR-01: only the avaliador desta avaliação pode avaliar as competências.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEvaluatorId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliador desta avaliação pode avaliar as competências");

    List<CompetencyItem> competencies = req.getCompetencies().stream()
        .map(dto -> {
          CompetencyCategory category = CompetencyCategory.fromCodeOrThrow(dto.getCategory());
          if (dto.getScore() != null) {
            return CompetencyItem.reconstruct(
                dto.getCompetencyCode(),
                dto.getCompetencyName(),
                category,
                dto.getScore()
            );
          } else {
            return CompetencyItem.create(
                dto.getCompetencyCode(),
                dto.getCompetencyName(),
                category
            );
          }
        })
        .collect(Collectors.toList());

    // In a normal cycle, we set/evaluate competencies
    SiadapEvaluation updated = evaluation.setCompetencies(competencies);
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
