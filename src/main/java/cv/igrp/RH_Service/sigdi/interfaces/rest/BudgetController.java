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
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import java.util.Map;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperCostDriverListDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateReqDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/budget")
@Tag(
    name = "Sigdi",
    description = "gest"
)
public class BudgetController {

  
  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public BudgetController(QueryBus queryBus, CommandBus commandBus) {
          this.queryBus = queryBus;
          this.commandBus = commandBus;
  }
   @GetMapping(
   value = "availability"
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

   @PostMapping(
   value = "sync/sigof"
  )
  @Operation(
    summary = "Sync sigof",
    description = "Sync sigof",
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
  
  public ResponseEntity<Map<String, ?>> syncSigof(
    )
  {

      final var command = new SyncSigofCommand();

      return commandBus.send(command);

  }

   @GetMapping(
   value = "cost_drivers/{id}"
  )
  @Operation(
    summary = "Get cost driver",
    description = "Get cost driver",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CostDriverResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CostDriverResponseDTO> getCostDriver(
    @PathVariable(value = "id") String id)
  {

      final var query = new GetCostDriverQuery(id);

      return queryBus.handle(query);

  }

   @GetMapping(
   value = "cost_drivers"
  )
  @Operation(
    summary = "Get all cost drivers",
    description = "Get all cost drivers",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperCostDriverListDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperCostDriverListDTO> getAllCostDrivers(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize)
  {

      final var query = new GetAllCostDriversQuery(pageNumber, pageSize);

      return queryBus.handle(query);

  }

   @PutMapping(
   value = "cost_drivers/{id}"
  )
  @Operation(
    summary = "Update cost driver",
    description = "Update cost driver",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CostDriverResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CostDriverResponseDTO> updateCostDriver(@Valid @RequestBody CostDriverRequestDTO updateCostDriverRequest
    , @PathVariable(value = "id") String id)
  {

      final var command = new UpdateCostDriverCommand(updateCostDriverRequest, id);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "cost_drivers"
  )
  @Operation(
    summary = "Add cost driver",
    description = "Add cost driver",
    responses = {
      @ApiResponse(
          responseCode = "201",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CostDriverResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CostDriverResponseDTO> addCostDriver(@Valid @RequestBody CostDriverRequestDTO addCostDriverRequest
    )
  {

      final var command = new AddCostDriverCommand(addCostDriverRequest);

      return commandBus.send(command);

  }

   @PostMapping(
   value = "calculator/simulate"
  )
  @Operation(
    summary = "Simulate cost based drivers",
    description = "Simulate cost based drivers",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CostDriverSimulateResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CostDriverSimulateResponseDTO> simulateCostBasedDrivers(@Valid @RequestBody CostDriverSimulateReqDTO simulateCostBasedDriversRequest
    )
  {

      final var command = new SimulateCostBasedDriversCommand(simulateCostBasedDriversRequest);

      return commandBus.send(command);

  }

}