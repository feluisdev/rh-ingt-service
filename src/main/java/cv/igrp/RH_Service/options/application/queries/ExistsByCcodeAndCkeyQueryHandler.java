package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class ExistsByCcodeAndCkeyQueryHandler implements QueryHandler<ExistsByCcodeAndCkeyQuery, ResponseEntity<Boolean>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(ExistsByCcodeAndCkeyQueryHandler.class);
  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public ExistsByCcodeAndCkeyQueryHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<Boolean> handle(ExistsByCcodeAndCkeyQuery query) {
    // TODO: Implement the query handling logic here
    var ccode = query.getCcode();
    var ckey = query.getCkey();
    if(ccode == null || ccode.isBlank()){
      throw new IllegalArgumentException("The field <ccode> is required");
    }
    if(ckey == null || ckey.isBlank()){
      throw new IllegalArgumentException("The field <ckey> is required");
    }
    var exists = optionRepository.existsByCkeyAndCcode(ckey, ccode);

    return ResponseEntity.ok(exists);
  }

}
