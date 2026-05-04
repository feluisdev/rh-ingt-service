package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.InstitutionEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.IAMUserProfileEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.IAMUserProfileEntity;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetTaticalActivitiesQueryHandler implements QueryHandler<GetTaticalActivitiesQuery, ResponseEntity<WrapperListTaticalActivityDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTaticalActivitiesQueryHandler.class);

  private final TacticalActivityRepository repository;
  private final InstitutionEntityRepository institutionRepository;
  private final IAMUserProfileEntityRepository iamUserRepository;

  public GetTaticalActivitiesQueryHandler(
      TacticalActivityRepository repository,
      InstitutionEntityRepository institutionRepository,
      IAMUserProfileEntityRepository iamUserRepository) {
    this.repository = repository;
    this.institutionRepository = institutionRepository;
    this.iamUserRepository = iamUserRepository;
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

    // Resolve names in bulk
    Set<UUID> orgUnitIds = page.getData().stream()
        .map(TacticalActivity::getOrganicUnitId)
        .filter(id -> id != null)
        .collect(Collectors.toSet());

    Set<UUID> userIds = page.getData().stream()
        .map(TacticalActivity::getResponsibleWho)
        .filter(id -> id != null)
        .collect(Collectors.toSet());

    Map<UUID, String> orgUnitNames = institutionRepository.findAllById(orgUnitIds).stream()
        .collect(Collectors.toMap(InstitutionEntity::getId, InstitutionEntity::getName));

    Map<UUID, String> userNames = iamUserRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(IAMUserProfileEntity::getId, IAMUserProfileEntity::getFullName));

    WrapperListTaticalActivityDTO response = new WrapperListTaticalActivityDTO();
    response.setPageNumber(page.getPageNumber());
    response.setPageSize(page.getPageSize());
    response.setTotalElements(page.getTotalElements());
    response.setTotalPages(page.getTotalPages());
    response.setFirst(page.isFirst());
    response.setLast(page.isLast());
    response.setData(page.getData().stream()
        .map(activity -> toResume(activity, orgUnitNames, userNames))
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

  private TaticalActivityResumeDTO toResume(
      TacticalActivity activity, 
      Map<UUID, String> orgUnitNames, 
      Map<UUID, String> userNames) {
    TaticalActivityResumeDTO dto = new TaticalActivityResumeDTO();
    dto.setId(activity.getId().getValor().getValor());
    dto.setStrategicGoalId(activity.getStrategicGoalId().getValor().getValor());
    dto.setTitle(activity.getTitle());
    
    if (activity.getResponsibleWho() != null) {
      dto.setResponsible_who(activity.getResponsibleWho().toString());
      dto.setResponsibleName(userNames.get(activity.getResponsibleWho()));
    }

    if (activity.getOrganicUnitId() != null) {
      dto.setOrganicUnitId(activity.getOrganicUnitId().toString());
      dto.setOrganicUnitName(orgUnitNames.get(activity.getOrganicUnitId()));
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
