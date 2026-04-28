package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class DeleteStrategyMapLinkCommandHandler implements CommandHandler<DeleteStrategyMapLinkCommand, ResponseEntity<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteStrategyMapLinkCommandHandler.class);

  private final StrategyMapLinkRepository mapLinkRepository;

  public DeleteStrategyMapLinkCommandHandler(StrategyMapLinkRepository mapLinkRepository) {
    this.mapLinkRepository = mapLinkRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<String> handle(DeleteStrategyMapLinkCommand command) {
    LOGGER.debug("DeleteStrategyMapLinkCommand : {}", command);

    StrategyMapLinkId linkId = StrategyMapLinkId.from(UUID.fromString(command.getId()));

    mapLinkRepository.findById(linkId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy map link not found"));

    mapLinkRepository.delete(linkId);

    return ResponseEntity.noContent().build();
  }
}