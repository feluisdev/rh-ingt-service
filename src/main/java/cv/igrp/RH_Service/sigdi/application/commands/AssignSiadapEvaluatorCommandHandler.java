package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Partial guard, decided by the operator on 2026-10-02: the avaliado cannot choose their own
 * evaluator. Any other authenticated user can still reassign it -- there is no permission for
 * this act yet, and adding one would block reassignment everywhere until IAM grants it.
 */
// ACTOR-CHECK: ENFORCED -- evaluation.employeeId; the avaliado may not change their own evaluator
@Component
@RequiredArgsConstructor
public class AssignSiadapEvaluatorCommandHandler
    implements CommandHandler<AssignSiadapEvaluatorCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignSiadapEvaluatorCommandHandler.class);

  private final SiadapEvaluationRepository evaluationRepository;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(AssignSiadapEvaluatorCommand command) {
    LOGGER.debug("AssignSiadapEvaluatorCommand: {}", command);

    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());
    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (currentEmployeeId.equals(evaluation.getEmployeeId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "O avaliado não pode alterar o seu próprio avaliador");

    String evaluatorId = command.getBody() != null ? command.getBody().getEvaluatorId() : null;
    if (evaluatorId == null || evaluatorId.isBlank()) {
        throw IgrpResponseStatusException.badRequest("O ID do avaliador é obrigatório");
    }

    try {
      funcionarioLookupPort.findById(UUID.fromString(evaluatorId))
          .orElseThrow(() -> IgrpResponseStatusException.badRequest("Colaborador avaliador não encontrado"));
    } catch (IllegalArgumentException ex) {
      throw IgrpResponseStatusException.badRequest("ID do avaliador inválido: formato UUID esperado");
    }

    SiadapEvaluation updated = evaluation.assignEvaluator(evaluatorId);
    SiadapEvaluation saved = evaluationRepository.save(updated);

    LOGGER.info("Avaliador da avaliação '{}' atualizado para '{}'", evalId.getValor(), evaluatorId);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());

    if (saved.getEvaluatorId() != null) {
      try {
        funcionarioLookupPort.findById(UUID.fromString(saved.getEvaluatorId()))
            .ifPresent(evaluator -> dto.setEvaluatorName(evaluator.getNomeCompleto()));
      } catch (Exception ignored) {}
    }
    if (saved.getEmployeeId() != null) {
      try {
        funcionarioLookupPort.findById(UUID.fromString(saved.getEmployeeId()))
            .ifPresent(emp -> dto.setEmployeeName(emp.getNomeCompleto()));
      } catch (Exception ignored) {}
    }

    return ResponseEntity.ok(dto);
  }
}