package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityResumeDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListTaticalActivityDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.TaticalActivityFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetTaticalActivitiesQueryHandler implements QueryHandler<GetTaticalActivitiesQuery, ResponseEntity<WrapperListTaticalActivityDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTaticalActivitiesQueryHandler.class);

  private final TacticalActivityRepository repository;
  private final OrganicaLookupPort organicaLookupPort;
  private final FuncionarioLookupPort funcionarioLookupPort;

  public GetTaticalActivitiesQueryHandler(TacticalActivityRepository repository,
      OrganicaLookupPort organicaLookupPort,
      FuncionarioLookupPort funcionarioLookupPort) {
    this.repository = repository;
    this.organicaLookupPort = organicaLookupPort;
    this.funcionarioLookupPort = funcionarioLookupPort;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListTaticalActivityDTO> handle(GetTaticalActivitiesQuery query) {

    LOGGER.debug("GetTaticalActivitiesQuery: {}", query);

    int pageNumber = parsePageNumber(query.getPageNumber());
    int pageSize = parsePageSize(query.getPageSize());

    TaticalActivityFilter filter = TaticalActivityFilter.builder()
        .pageNumber(pageNumber)
        .pageSize(pageSize)
        .build();

    var page = repository.findAll(filter);
    var activities = page.getData();

    // Batch lookup — collect unique IDs first, then fetch in one call per BC
    Set<UUID> organicUnitIds = activities.stream()
        .map(TacticalActivity::getOrganicUnitId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Set<UUID> responsibleIds = activities.stream()
        .map(TacticalActivity::getResponsibleWho)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    Map<UUID, OrganicaDTO> organicaMap = organicaLookupPort.findAllByIds(organicUnitIds);
    Map<UUID, FuncionarioDTO> funcionarioMap = funcionarioLookupPort.findAllByIds(responsibleIds);

    WrapperListTaticalActivityDTO response = new WrapperListTaticalActivityDTO();
    response.setPageNumber(page.getPageNumber());
    response.setPageSize(page.getPageSize());
    response.setTotalElements(page.getTotalElements());
    response.setTotalPages(page.getTotalPages());
    response.setFirst(page.isFirst());
    response.setLast(page.isLast());
    response.setData(activities.stream()
        .map(a -> toResume(a, organicaMap, funcionarioMap))
        .toList());

    return ResponseEntity.ok(response);
  }

  private int parsePageNumber(String value) {
    if (value == null || value.trim().isEmpty()) {
      return 0;
    }
    try {
      int parsed = Integer.parseInt(value);
      if (parsed < 0) throw new NumberFormatException("pageNumber must be >= 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageNumber inválido");
    }
  }

  private int parsePageSize(String value) {
    if (value == null || value.trim().isEmpty()) {
      return 20;
    }
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0) throw new NumberFormatException("pageSize must be > 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageSize inválido");
    }
  }

  private TaticalActivityResumeDTO toResume(TacticalActivity activity,
      Map<UUID, OrganicaDTO> organicaMap, Map<UUID, FuncionarioDTO> funcionarioMap) {
    TaticalActivityResumeDTO dto = new TaticalActivityResumeDTO();
    dto.setId(activity.getId().getValor().getValor());
    dto.setStrategicGoalId(activity.getStrategicGoalId().getValor().getValor());
    dto.setTitle(activity.getTitle());

    if (activity.getResponsibleWho() != null) {
      dto.setResponsible_who(activity.getResponsibleWho().toString());
      FuncionarioDTO func = funcionarioMap.get(activity.getResponsibleWho());
      if (func != null) dto.setResponsibleName(func.getNomeCompleto());
    }

    if (activity.getOrganicUnitId() != null) {
      dto.setOrganicUnitId(activity.getOrganicUnitId().toString());
      OrganicaDTO organica = organicaMap.get(activity.getOrganicUnitId());
      if (organica != null) dto.setOrganicUnitName(organica.getName());
    }

    if (activity.getBudget() != null) {
      dto.setBudget_estimated(activity.getBudget().getEstimatedAmount());
    }

    dto.setStart_date(activity.getDateRange().getStartDate().toString());
    dto.setEnd_date(activity.getDateRange().getEndDate().toString());
    dto.setStatus(activity.getStatus().getCode());
    dto.setStatusDesc(activity.getStatus().getDescription());
    return dto;
  }

}
