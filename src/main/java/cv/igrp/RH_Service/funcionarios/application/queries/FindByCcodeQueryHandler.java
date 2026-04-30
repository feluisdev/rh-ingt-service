package cv.igrp.RH_Service.funcionarios.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.funcionarios.application.dto.OptionCodeItemDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionCodeResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.filter.OptionFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Option;
import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

@Component("funcionariosFindByCcodeQueryHandler")
public class FindByCcodeQueryHandler implements QueryHandler<FindByCcodeQuery, ResponseEntity<OptionCodeResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(FindByCcodeQueryHandler.class);

  private final OptionRepository optionRepository;

  public FindByCcodeQueryHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

   @IgrpQueryHandler
  public ResponseEntity<OptionCodeResponseDTO> handle(FindByCcodeQuery query) {
    LOGGER.info("Buscando Options por ccode={} e locale={}", query.getCcode(), query.getLocale());

    OptionFilter filter = OptionFilter.builder()
        .ccode(query.getCcode())
        .locale(query.getLocale())
        .pageNumber(0)
        .pageSize(20)
        .build();

    var options = optionRepository.getAll(filter);

    if (options == null || options.isEmpty()) {
      throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,
          "Option não encontrado para ccode: " + query.getCcode());
    }

    var items = options.stream().map(opt -> {
      OptionCodeItemDTO dto = new OptionCodeItemDTO();
      dto.setId(opt.getIdOption() != null ? opt.getIdOption().getStringValor() : null);
      dto.setCcode(opt.getCcode());
      dto.setCkey(opt.getCkey());
      dto.setCvalue(opt.getCvalue());
      dto.setDescription(opt.getDescription());
      return dto;
    }).toList();

    OptionCodeResponseDTO responseDTO = new OptionCodeResponseDTO();
    responseDTO.setCode(query.getCcode());
    responseDTO.setLocale(query.getLocale());
    responseDTO.setItems(items);

    return ResponseEntity.ok(responseDTO);
  }

}
