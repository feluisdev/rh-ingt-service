package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IndividualObjectiveDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CompetencyItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetEvaluationDetailQueryHandler
    implements QueryHandler<GetEvaluationDetailQuery, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final OrganicaLookupPort organicaLookupPort;
  private final SiadapEvaluationMapper mapper;

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(GetEvaluationDetailQuery query) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(query.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação de desempenho não encontrada"));

    SiadapEvaluationDTO dto = mapper.toDto(mapper.toEntity(evaluation));

    // Populate objectives
    List<IndividualObjectiveDTO> objectives = evaluation.getObjectives().stream()
        .map(obj -> new IndividualObjectiveDTO(
            obj.getCode(),
            obj.getDescription(),
            obj.getIndicator(),
            obj.getTargetValue(),
            obj.getAchievedValue(),
            obj.getScore(),
            obj.getWeight()
        ))
        .collect(Collectors.toList());
    dto.setObjectives(objectives);

    // Populate competencies
    List<CompetencyItemDTO> competencies = evaluation.getCompetencies().stream()
        .map(comp -> new CompetencyItemDTO(
            comp.getCompetencyCode(),
            comp.getCompetencyName(),
            comp.getCategory().getCode(),
            comp.getScore()
        ))
        .collect(Collectors.toList());
    dto.setCompetencies(competencies);

    // Lookup employee name
    try {
      funcionarioLookupPort.findById(UUID.fromString(evaluation.getEmployeeId()))
          .ifPresent(emp -> dto.setEmployeeName(emp.getNomeCompleto()));
    } catch (Exception e) {
      // Ignored: lookup issue
    }

    // Lookup organic unit name
    if (evaluation.getOrganicUnitId() != null) {
      try {
        organicaLookupPort.findById(UUID.fromString(evaluation.getOrganicUnitId()))
            .ifPresent(org -> dto.setOrganicUnitName(org.getName()));
      } catch (Exception e) {
        // Ignored
      }
    }

    dto.setPhase(evaluation.getPhase().getCode());
    dto.setStatus(evaluation.isValidatedQuota() ? "CLOSED" : "DRAFT");

    return ResponseEntity.ok(dto);
  }
}
