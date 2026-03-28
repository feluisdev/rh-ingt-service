package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;

@Component
public class GetKeyResultQueryHandler implements QueryHandler<GetKeyResultQuery, ResponseEntity<KeyResultResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetKeyResultQueryHandler.class);

  private final KeyResultRepository repository;

  public GetKeyResultQueryHandler(KeyResultRepository repository) {
    this.repository = repository;
  }

   @IgrpQueryHandler
  public ResponseEntity<KeyResultResponseDTO> handle(GetKeyResultQuery query) {

    LOGGER.debug("GetKeyResultQuery: {}", query);

    var keyResult = repository.findByIdFull(KeyResultId.from(query.getId()))
        .orElseThrow(() -> IgrpResponseStatusException.notFound("KeyResult não encontrado"));

    return ResponseEntity.ok(toResponse(keyResult));
  }

  private KeyResultResponseDTO toResponse(cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult kr) {
    KeyResultResponseDTO dto = new KeyResultResponseDTO();
    dto.setId(kr.getId().getValor().getValor());
    dto.setTitle(kr.getTitle());
    dto.setTargetValue(kr.getTargetValue());
    dto.setCurrentValue(kr.getCurrentValue());
    dto.setMetricUnit(kr.getMetricUnit() != null ? kr.getMetricUnit().getCode() : null);
    dto.setActivityId(kr.getActivityId().getValor().getValor());
    return dto;
  }

}
