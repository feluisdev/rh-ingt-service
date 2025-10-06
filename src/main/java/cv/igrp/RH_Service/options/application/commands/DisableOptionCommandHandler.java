package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class DisableOptionCommandHandler implements CommandHandler<DisableOptionCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisableOptionCommandHandler.class);
  private final OptionRepository optionRepository;


  public DisableOptionCommandHandler(OptionRepository optionRepository) {
    this.optionRepository = optionRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(DisableOptionCommand command) {
    // TODO: Implement the command handling logic here
    var optionId = OptionId.from(command.getOptionId());

    var existing = optionRepository.findById(optionId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Option not found for id: " + command.getOptionId()));

    existing.disable();

    optionRepository.save(existing);

    return ResponseEntity.ok(Map.of("message","Option disable successfully"));

  }

}
