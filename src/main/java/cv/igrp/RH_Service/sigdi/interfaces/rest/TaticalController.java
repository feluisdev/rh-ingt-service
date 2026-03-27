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
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListTaticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityStatusDTO;
import java.util.Map;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;

@IgrpController
@RestController
@RequestMapping(path = "tactical")
@Tag(
    name = "Sigdi",
    description = "gest strategies"
)
public class TaticalController {

  
  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public TaticalController(QueryBus queryBus, CommandBus commandBus) {
          this.queryBus = queryBus;
          this.commandBus = commandBus;
  }
   @GetMapping(
   value = "activities"
  )
  @Operation(
    summary = "Get tatical activities",
    description = "Get tatical activities",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListTaticalActivityDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListTaticalActivityDTO> getTaticalActivities(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false) String pageSize,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "unidade", required = false) String unidade,
    @RequestParam(value = "data", required = false) String data)
  {

      final var query = new GetTaticalActivitiesQuery(pageNumber, pageSize, status, unidade, data);

      return queryBus.handle(query);

  }

   @PostMapping(
   value = "activities"
  )
  @Operation(
    summary = "Create tactical activity",
    description = "Create tactical activity",
    responses = {
      @ApiResponse(
          responseCode = "201",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TacticalActivityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<TacticalActivityResponseDTO> createTacticalActivity(@Valid @RequestBody CreateTacticalActivityDTO createTacticalActivityRequest
    )
  {

      final var command = new CreateTacticalActivityCommand(createTacticalActivityRequest);

      return commandBus.send(command);

  }

   @PatchMapping(
   value = "activities/{id}/status"
  )
  @Operation(
    summary = "Change status tactical activity",
    description = "Change status tactical activity",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<Map<String, ?>> changeStatusTacticalActivity(@Valid @RequestBody TaticalActivityStatusDTO changeStatusTacticalActivityRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new ChangeStatusTacticalActivityCommand(changeStatusTacticalActivityRequest, id);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "krs/{id}/checkin"
  )
  @Operation(
    summary = "Registra processo kr",
    description = "Registra processo kr",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<String> registraProcessoKr(@Valid @RequestBody KeyResultCheckinRequestDTO registraProcessoKrRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new RegistraProcessoKrCommand(registraProcessoKrRequest, id);

      return commandBus.send(command);

  }

}