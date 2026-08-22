package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.specifications.SiadapEvaluationSpecifications;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperSiadapEvaluationListDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ListSiadapEvaluationsQueryHandler
    implements QueryHandler<ListSiadapEvaluationsQuery, ResponseEntity<WrapperSiadapEvaluationListDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListSiadapEvaluationsQueryHandler.class);

  private final SiadapEvaluationEntityRepository repository;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final OrganicaLookupPort organicaLookupPort;

  public ListSiadapEvaluationsQueryHandler(SiadapEvaluationEntityRepository repository,
                                           FuncionarioLookupPort funcionarioLookupPort,
                                           OrganicaLookupPort organicaLookupPort) {
    this.repository = repository;
    this.funcionarioLookupPort = funcionarioLookupPort;
    this.organicaLookupPort = organicaLookupPort;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<WrapperSiadapEvaluationListDTO> handle(ListSiadapEvaluationsQuery query) {
    LOGGER.debug("ListSiadapEvaluationsQuery: {}", query);

    int page = parseIntOrDefault(query.getPageNumber(), 0);
    int size = parseIntOrDefault(query.getPageSize(), 20);

    String statusParam = query.getStatus();
    EvaluationPhase phase = (statusParam == null || statusParam.isBlank())
        ? null
        : EvaluationPhase.fromCodeOrThrow(statusParam);

    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(query.getYear(), query.getOrganicUnitId(), phase);
    Page<SiadapEvaluationEntity> pageResult = repository.findAll(spec, PageRequest.of(page, size));

    List<SiadapEvaluationDTO> data = pageResult.getContent().stream()
        .map(this::toDto)
        .collect(Collectors.toList());

    int totalPages = size > 0 ? (int) Math.ceil((double) pageResult.getTotalElements() / size) : 0;

    WrapperSiadapEvaluationListDTO response = new WrapperSiadapEvaluationListDTO();
    response.setData(data);
    response.setPage(page);
    response.setSize(size);
    response.setTotalElements(pageResult.getTotalElements());
    response.setTotalPages(totalPages);

    return ResponseEntity.ok(response);
  }

  private SiadapEvaluationDTO toDto(SiadapEvaluationEntity e) {
    SiadapEvaluationDTO dto = new SiadapEvaluationDTO();
    dto.setId(e.getId().toString());
    dto.setEmployeeId(e.getEmployeeId());
    dto.setYear(e.getYear());
    dto.setObjectivesScore(e.getObjectivesScore());
    dto.setCompetenciesScore(e.getCompetenciesScore());
    dto.setFinalScore(e.getFinalScore());
    dto.setMeritRating(e.getMeritRating());
    dto.setQuotaValidated(e.isValidatedQuota());
    dto.setStatus(e.isValidatedQuota() ? "CLOSED" : "DRAFT");
    dto.setLastUpdatedAt(e.getLastModifiedDate() != null ? e.getLastModifiedDate().toString() : null);

    dto.setOrganicUnitId(e.getOrganicUnitId());
    dto.setEvaluatorId(e.getEvaluatorId());
    dto.setPhase(e.getEvaluationPhase());
    dto.setSelfEvaluationTacitlyAccepted(e.isSelfEvaluationTacitlyAccepted());
    dto.setSelfEvaluationScore(e.getSelfEvaluationScore());
    dto.setResultsWeight(e.getResultsWeight());
    dto.setCompetenciesWeight(e.getCompetenciesWeight());

    // Lookup employee name
    try {
      funcionarioLookupPort.findById(UUID.fromString(e.getEmployeeId()))
          .ifPresent(emp -> dto.setEmployeeName(emp.getNomeCompleto()));
    } catch (Exception ex) {
      dto.setEmployeeName("Colaborador " + e.getEmployeeId());
    }

    // Lookup organic unit name
    if (e.getOrganicUnitId() != null) {
      try {
        organicaLookupPort.findById(UUID.fromString(e.getOrganicUnitId()))
            .ifPresent(org -> dto.setOrganicUnitName(org.getName()));
      } catch (Exception ex) {
        dto.setOrganicUnitName(null);
      }
    }

    return dto;
  }

  private int parseIntOrDefault(String value, int defaultValue) {
    if (value == null || value.isBlank()) return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }
}
