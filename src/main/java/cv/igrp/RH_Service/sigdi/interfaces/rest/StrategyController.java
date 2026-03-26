/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.CreateIdentityRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "ap1/v1/strategy")
@Tag(
    name = "Sigdi",
    description = "gest strategies"
)
public class StrategyController {

  
  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public StrategyController(QueryBus queryBus, CommandBus commandBus) {
          this.queryBus = queryBus;
          this.commandBus = commandBus;
  }
   @PutMapping(
   value = "identities/current"
  )
  @Operation(
    summary = "Create identitie",
    description = "Create identitie",
    responses = {
      @ApiResponse(
          responseCode = "201",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = IdentityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<IdentityResponseDTO> createIdentitie(@Valid @RequestBody CreateIdentityRequestDTO createIdentitieRequest
    )
  {

      final var command = new CreateIdentitieCommand(createIdentitieRequest);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "goals"
  )
  @Operation(
    summary = "Create strategic goal",
    description = "Create strategic goal",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = StategicGoalResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<StategicGoalResponseDTO> createStrategicGoal(@Valid @RequestBody CreateStategicGoalDTO createStrategicGoalRequest
    )
  {

      final var command = new CreateStrategicGoalCommand(createStrategicGoalRequest);

      return commandBus.send(command);

  }

   @GetMapping(
   value = "identities/current"
  )
  @Operation(
    summary = "Get current identitie",
    description = "Get current identitie",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = IdentityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<IdentityResponseDTO> getCurrentIdentitie(
    )
  {

      final var query = new GetCurrentIdentitieQuery();

      return queryBus.handle(query);

  }

}