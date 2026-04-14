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
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapDataDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListIdentitieDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListStrategyGoalsDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateStategicGoalDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/strategy")
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
   @PostMapping(
   value = "identities"
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

   @PostMapping(
   value = "map/links"
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
  
  public ResponseEntity<StrategyMapLinkResponseDTO> createStrategyMapLink(@Valid @RequestBody StrategyLinkDTO createStrategyMapLinkRequest
    )
  {

      final var command = new CreateStrategyMapLinkCommand(createStrategyMapLinkRequest);

      return commandBus.send(command);

  }

   @GetMapping(
   value = "map/current"
  )
  @Operation(
    summary = "Get current strategy map",
    description = "Get current strategy map",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = StrategyMapDataDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<StrategyMapDataDTO> getCurrentStrategyMap(
    )
  {

      final var query = new GetCurrentStrategyMapQuery();

      return queryBus.handle(query);

  }

   @GetMapping(
   value = "identities"
  )
  @Operation(
    summary = "Get list identitie",
    description = "Get list identitie",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListIdentitieDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListIdentitieDTO> getListIdentitie(
    @RequestParam(value = "cicleYear", required = false) String cicleYear,
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize)
  {

      final var query = new GetListIdentitieQuery(cicleYear, pageNumber, pageSize);

      return queryBus.handle(query);

  }

   @GetMapping(
   value = "goals"
  )
  @Operation(
    summary = "Get list strategic goals",
    description = "Get list strategic goals",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListStrategyGoalsDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListStrategyGoalsDTO> getListStrategicGoals(
    @RequestParam(value = "perspective", required = false) String perspective,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "parentGoalId", required = false) String parentGoalId,
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize)
  {

      final var query = new GetListStrategicGoalsQuery(perspective, status, parentGoalId, pageNumber, pageSize);

      return queryBus.handle(query);

  }

   @PatchMapping(
   value = "goals/{id}"
  )
  @Operation(
    summary = "Update strategic goals",
    description = "Update strategic goals",
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
  
  public ResponseEntity<StategicGoalResponseDTO> updateStrategicGoals(@Valid @RequestBody UpdateStategicGoalDTO updateStrategicGoalsRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new UpdateStrategicGoalsCommand(updateStrategicGoalsRequest, id);

      return commandBus.send(command);

  }

   @DeleteMapping(
   value = "map/links/{id}"
  )
  @Operation(
    summary = "Delete strategy map link",
    description = "Delete strategy map link",
    responses = {
      @ApiResponse(
          responseCode = "204",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<String> deleteStrategyMapLink(
    @PathVariable(value = "id") String id)
  {

      final var command = new DeleteStrategyMapLinkCommand(id);

      return commandBus.send(command);

  }

}