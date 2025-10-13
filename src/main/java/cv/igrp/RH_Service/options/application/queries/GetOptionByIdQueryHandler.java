package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;

@Component
public class GetOptionByIdQueryHandler implements QueryHandler<GetOptionByIdQuery, ResponseEntity<OptionResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetOptionByIdQueryHandler.class);
  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public GetOptionByIdQueryHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<OptionResponseDTO> handle(GetOptionByIdQuery query) {

    var optionId = query.getOptionId();

    if (optionId == null || optionId.isBlank()) {
      throw IgrpResponseStatusException.badRequest("The field <optionId> is required");
    }
    var sector = optionRepository.findById(OptionId.from(optionId))
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Sector with id '" + optionId + "' not found"));

    var responseDTO = optionMapper.toResponseDTO(sector);

    return ResponseEntity.ok(responseDTO);
  }

}
