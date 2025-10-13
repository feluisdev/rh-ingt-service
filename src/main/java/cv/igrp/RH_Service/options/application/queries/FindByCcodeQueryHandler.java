package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.RH_Service.options.domain.filter.OptionFilter;
import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.OptionCodeResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FindByCcodeQueryHandler implements QueryHandler<FindByCcodeQuery, ResponseEntity<OptionCodeResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(FindByCcodeQueryHandler.class);
  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public FindByCcodeQueryHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<OptionCodeResponseDTO> handle(FindByCcodeQuery query) {
    String ccode = query.getCcode();

    if (ccode == null || ccode.isBlank()) {
      throw IgrpResponseStatusException.badRequest("The field <ccode> is required");
    }

    LOGGER.info("Handling FindByCcodeQuery with ccode: {}", ccode);

    String locale = (query.getLocale() != null && !query.getLocale().isBlank())
        ? query.getLocale()
        : "pt-CV";

    // Criar filtro pelo ccode
    OptionFilter filter = OptionFilter.builder()
        .ccode(ccode)
        .build();

    // Buscar as opções no repositório
    var options = optionRepository.findByCcode(ccode);

    if (options == null || options.isEmpty()) {
      throw IgrpResponseStatusException.notFound("No options found for ccode: " + ccode);
    }

    // Mapear para DTO
    var content = options.stream()
        .map(optionMapper::toOptionCodeItemDTO)
        .toList();

    // Montar resposta
    OptionCodeResponseDTO response = new OptionCodeResponseDTO();
    response.setCode(ccode);
    response.setLocale(locale);
    response.setItems(content);


    return ResponseEntity.ok(response);
  }

}
