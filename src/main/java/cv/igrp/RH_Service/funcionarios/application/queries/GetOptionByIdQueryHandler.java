package cv.igrp.RH_Service.funcionarios.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.funcionarios.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

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
    var optionId = ExternalID.from(query.getOptionId());

    var option = optionRepository.getById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,
            "Option não encontrado com ID: " + optionId.getStringValor()));

    var dto = optionMapper.toDTO(option);
    return ResponseEntity.ok(dto);
  }

}