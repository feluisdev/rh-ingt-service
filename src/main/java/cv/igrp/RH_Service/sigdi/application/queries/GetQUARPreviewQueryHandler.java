package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.OkrEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.InstitutionalIdentityEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.OkrEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.QUARBudgetSummaryDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARCompletenessDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARKeyResultDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARMissingCheckinDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARObjectiveDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QUARPreviewResponseDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetQUARPreviewQueryHandler
    implements QueryHandler<GetQUARPreviewQuery, ResponseEntity<QUARPreviewResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQUARPreviewQueryHandler.class);

  private final InstitutionalIdentityEntityRepository identityRepository;
  private final OkrEntityRepository okrRepository;

  public GetQUARPreviewQueryHandler(InstitutionalIdentityEntityRepository identityRepository,
                                     OkrEntityRepository okrRepository) {
    this.identityRepository = identityRepository;
    this.okrRepository = okrRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<QUARPreviewResponseDTO> handle(GetQUARPreviewQuery query) {
    LOGGER.debug("GetQUARPreviewQuery: {}", query);

    InstitutionalIdentityEntity identity = identityRepository.findFirstByIsActiveTrue()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("No active institution found"));

    String cycle = query.getYear().toString();
    List<OkrEntity> okrs = okrRepository.findAllByInstitutionIdAndCycle(identity.getInstitutionId(), cycle);

    // Build completeness
    List<QUARMissingCheckinDTO> missing = new ArrayList<>();
    int withCheckins = 0;

    for (OkrEntity okr : okrs) {
      boolean hasCheckin = okr.getKeyResults().stream()
          .anyMatch(kr -> !kr.getKeyResultCheckins().isEmpty());
      if (hasCheckin) {
        withCheckins++;
      } else {
        QUARMissingCheckinDTO m = new QUARMissingCheckinDTO();
        m.setOkrId(okr.getId().toString());
        m.setTitle(okr.getTitle());
        missing.add(m);
      }
    }

    QUARCompletenessDTO completeness = new QUARCompletenessDTO();
    completeness.setTotalObjectives(okrs.size());
    completeness.setObjectivesWithCheckins(withCheckins);
    completeness.setMissingCheckins(missing);

    // Build objectives
    List<QUARObjectiveDTO> objectives = okrs.stream()
        .map(okr -> buildObjective(okr))
        .collect(Collectors.toList());

    // Budget summary — placeholder (allocated comes from SIGOF sync)
    QUARBudgetSummaryDTO budgetSummary = new QUARBudgetSummaryDTO();
    budgetSummary.setAllocated(BigDecimal.ZERO);
    budgetSummary.setExecuted(BigDecimal.ZERO);
    budgetSummary.setExecutionRate(BigDecimal.ZERO);

    QUARPreviewResponseDTO response = new QUARPreviewResponseDTO();
    response.setInstitutionId(identity.getInstitutionId() != null ? identity.getInstitutionId().toString() : null);
    response.setInstitutionName(identity.getMission());
    response.setYear(query.getYear());
    response.setGeneratedAt(LocalDateTime.now().toString());
    response.setCompleteness(completeness);
    response.setObjectives(objectives);
    response.setBudgetSummary(budgetSummary);

    return ResponseEntity.ok(response);
  }

  private QUARObjectiveDTO buildObjective(OkrEntity okr) {
    List<KeyResultsEntity> krs = okr.getKeyResults();

    BigDecimal totalTarget = BigDecimal.ZERO;
    BigDecimal totalCurrent = BigDecimal.ZERO;
    List<QUARKeyResultDTO> krDtos = new ArrayList<>();

    for (KeyResultsEntity kr : krs) {
      BigDecimal target = kr.getTargetValue() != null ? kr.getTargetValue() : BigDecimal.ZERO;
      BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
      BigDecimal deviation = current.subtract(target);

      totalTarget = totalTarget.add(target);
      totalCurrent = totalCurrent.add(current);

      QUARKeyResultDTO krDto = new QUARKeyResultDTO();
      krDto.setId(kr.getId().toString());
      krDto.setTitle(kr.getTitle());
      krDto.setUnit(kr.getMetricUnit());
      krDto.setPlanned(target);
      krDto.setRealized(current);
      krDto.setDeviation(deviation);
      krDtos.add(krDto);
    }

    BigDecimal deviation = totalCurrent.subtract(totalTarget);
    BigDecimal deviationPct = totalTarget.compareTo(BigDecimal.ZERO) != 0
        ? deviation.divide(totalTarget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
        : BigDecimal.ZERO;

    String rating = computeRating(deviationPct);

    QUARObjectiveDTO dto = new QUARObjectiveDTO();
    dto.setId(okr.getId().toString());
    dto.setTitle(okr.getTitle());
    dto.setPlanned(totalTarget);
    dto.setRealized(totalCurrent);
    dto.setDeviation(deviation);
    dto.setDeviationPct(deviationPct.setScale(1, RoundingMode.HALF_UP));
    dto.setRating(rating);
    dto.setKeyResults(krDtos);
    return dto;
  }

  private String computeRating(BigDecimal deviationPct) {
    if (deviationPct.compareTo(new BigDecimal("-20")) < 0) return "RED";
    if (deviationPct.compareTo(new BigDecimal("-5")) < 0) return "YELLOW";
    return "GREEN";
  }
}
