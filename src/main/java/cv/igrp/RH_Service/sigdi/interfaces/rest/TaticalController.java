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
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;

@IgrpController
@RestController
@RequestMapping(path = "tactical/activities")
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
    @RequestParam(value = "pageSize", required = false) String pageSize)
  {

      final var query = new GetTaticalActivitiesQuery(pageNumber, pageSize);

      return queryBus.handle(query);

  }

   @PostMapping(
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

   @GetMapping(
   value = "budget"
  )
  @Operation(
    summary = "Get budget",
    description = "Get budget",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = BudgetInfoDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<BudgetInfoDTO> getBudget(
    @RequestParam(value = "economicClassifier") String economicClassifier)
  {

      final var query = new GetBudgetQuery(economicClassifier);

      return queryBus.handle(query);

  }

}