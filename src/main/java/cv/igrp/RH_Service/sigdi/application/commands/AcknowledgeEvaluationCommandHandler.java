package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AcknowledgeEvaluationCommandHandler
    implements CommandHandler<AcknowledgeEvaluationCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AcknowledgeEvaluationCommandHandler.class);

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(AcknowledgeEvaluationCommand command) {
    LOGGER.debug("AcknowledgeEvaluationCommand: {}", command);

    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());
    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    if (command.getBody() == null || command.getBody().getAgreed() == null) {
        throw IgrpResponseStatusException.badRequest("O campo agreed é obrigatório");
    }

    boolean agreed = Boolean.TRUE.equals(command.getBody().getAgreed());
    String comment = command.getBody().getComment();

    SiadapEvaluation updated = evaluation.acknowledge(agreed, comment);
    SiadapEvaluation saved = evaluationRepository.save(updated);

    LOGGER.info("Tomada de conhecimento/contraditório registado para avaliação '{}': agreed={}, status={}",
        evalId.getValor(), agreed, saved.getAcknowledgementStatus());

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
