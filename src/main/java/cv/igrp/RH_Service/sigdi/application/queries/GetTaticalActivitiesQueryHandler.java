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

@Component
public class GetTaticalActivitiesQueryHandler implements QueryHandler<GetTaticalActivitiesQuery, ResponseEntity<WrapperListTaticalActivityDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTaticalActivitiesQueryHandler.class);

  private final TacticalActivityRepository repository;

  public GetTaticalActivitiesQueryHandler(TacticalActivityRepository repository) {
    this.repository = repository;
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

    WrapperListTaticalActivityDTO response = new WrapperListTaticalActivityDTO();
    response.setPageNumber(page.getPageNumber());
    response.setPageSize(page.getPageSize());
    response.setTotalElements(page.getTotalElements());
    response.setTotalPages(page.getTotalPages());
    response.setFirst(page.isFirst());
    response.setLast(page.isLast());
    response.setData(page.getData().stream().map(this::toResume).toList());

    return ResponseEntity.ok(response);
  }

  private int parsePageNumber(String value) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed < 0) throw new NumberFormatException("pageNumber must be >= 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageNumber inválido");
    }
  }

  private int parsePageSize(String value) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0) throw new NumberFormatException("pageSize must be > 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageSize inválido");
    }
  }

  private TaticalActivityResumeDTO toResume(TacticalActivity activity) {
    TaticalActivityResumeDTO dto = new TaticalActivityResumeDTO();
    dto.setId(activity.getId().getValor().getValor());
    dto.setStrategicGoalId(activity.getStrategicGoalId().getValor().getValor());
    dto.setTitle(activity.getTitle());
    dto.setResponsible_who(activity.getResponsibleWho());
    dto.setBudget_estimated(activity.getBudget().getEstimatedAmount());
    dto.setStart_date(activity.getDateRange().getStartDate().toString());
    dto.setEnd_date(activity.getDateRange().getEndDate().toString());
    dto.setStatus(activity.getStatus().getCode());
    dto.setStatusDesc(activity.getStatus().getDescription());
    return dto;
  }

}
