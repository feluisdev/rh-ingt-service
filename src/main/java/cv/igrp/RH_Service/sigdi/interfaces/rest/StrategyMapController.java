/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.RH_Service.sigdi.application.commands.CreateStrategyMapLinkCommand;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@IgrpController
@RestController
@RequestMapping(path = "strategy/map")
@Tag(
    name = "Sigdi",
    description = "strategy map links"
)
public class StrategyMapController {

  private final CommandBus commandBus;

  public StrategyMapController(CommandBus commandBus) {
    this.commandBus = commandBus;
  }

  @PostMapping(
      value = "links"
  )
  @Operation(
      summary = "Create strategy map link",
      description = "Create strategy map link",
      responses = {
          @ApiResponse(
              responseCode = "201",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(
                      implementation = StrategyMapLinkResponseDTO.class,
                      type = "object")
              )
          )
      }
  )
  public ResponseEntity<StrategyMapLinkResponseDTO> createLink(@Valid @RequestBody StrategyLinkDTO request) {
    final var command = new CreateStrategyMapLinkCommand(request);
    return commandBus.send(command);
  }
}

