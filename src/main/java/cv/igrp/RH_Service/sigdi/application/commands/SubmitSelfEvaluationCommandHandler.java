package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SubmitSelfEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubmitSelfEvaluationCommandHandler
    implements CommandHandler<SubmitSelfEvaluationCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(SubmitSelfEvaluationCommand command) {
    SubmitSelfEvaluationRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(req.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    SiadapEvaluation updated = evaluation.submitSelfEvaluation(req.getSelfEvaluationScore());
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toDto(mapper.toEntity(saved));
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
