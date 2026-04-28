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
import jakarta.validation.Valid;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetAvailabilityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateReqDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SyncSigofRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SyncJobResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SyncJobStatusResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperCostDriverListDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/budget")
@Tag(
    name = "SIGDI-Budget",
    description = "Budget management"
)
public class BudgetController {

  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public BudgetController(QueryBus queryBus, CommandBus commandBus) {
    this.queryBus = queryBus;
    this.commandBus = commandBus;
  }

  @GetMapping(value = "availability")
  @Operation(
    summary = "Get budget availability",
    description = "Consulta o saldo disponível para um classificador e unidade orgânica",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BudgetAvailabilityResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<BudgetAvailabilityResponseDTO> getBudgetAvailability(
    @RequestParam(value = "classifier") String classifier,
    @RequestParam(value = "organicUnitId") String organicUnitId,
    @RequestParam(value = "fiscalYear") Integer fiscalYear,
    @RequestParam(value = "requestedAmount", required = false) String requestedAmount)
  {
    final var query = new GetBudgetAvailabilityQuery(classifier, organicUnitId, fiscalYear, requestedAmount);
    return queryBus.handle(query);
  }

  @GetMapping(value = "summary")
  @Operation(
    summary = "Get budget summary",
    description = "Retorna o resumo de execução orçamental agrupado por classificador",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BudgetSummaryResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<BudgetSummaryResponseDTO> getBudgetSummary(
    @RequestParam(value = "fiscalYear", required = false) String fiscalYear,
    @RequestParam(value = "organicUnitId", required = false) String organicUnitId)
  {
    final var query = new GetBudgetSummaryQuery(fiscalYear, organicUnitId);
    return queryBus.handle(query);
  }

  @PostMapping(value = "sync/sigof")
  @Operation(
    summary = "Sync SIGOF",
    description = "Força a sincronização manual com o SIGOF (assíncrono)",
    responses = {
      @ApiResponse(
          responseCode = "202",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SyncJobResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SyncJobResponseDTO> syncSigof(
    @Valid @RequestBody SyncSigofRequestDTO syncSigofRequest)
  {
    final var command = new SyncSigofCommand(syncSigofRequest);
    return commandBus.send(command);
  }

  @GetMapping(value = "sync/sigof/{jobId}")
  @Operation(
    summary = "Get sync job status",
    description = "Consulta o estado de uma sincronização manual",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SyncJobStatusResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SyncJobStatusResponseDTO> getSyncJobStatus(
    @PathVariable(value = "jobId") String jobId)
  {
    final var query = new GetSyncJobStatusQuery(jobId);
    return queryBus.handle(query);
  }

  @PostMapping(value = "calculator/simulate")
  @Operation(
    summary = "Simulate cost based drivers",
    description = "Calcula o custo de uma despesa com base em parâmetros oficiais",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CostDriverSimulateResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<CostDriverSimulateResponseDTO> simulateCostBasedDrivers(
    @Valid @RequestBody CostDriverSimulateReqDTO simulateCostBasedDriversRequest)
  {
    final var command = new SimulateCostBasedDriversCommand(simulateCostBasedDriversRequest);
    return commandBus.send(command);
  }

  @GetMapping(value = "cost_drivers/{id}")
  @Operation(
    summary = "Get cost driver",
    description = "Get cost driver",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CostDriverResponseDTO.class, type = "object")
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

  @GetMapping(value = "cost_drivers")
  @Operation(
    summary = "Get all cost drivers",
    description = "Get all cost drivers",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = WrapperCostDriverListDTO.class, type = "object")
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

  @PutMapping(value = "cost_drivers/{id}")
  @Operation(
    summary = "Update cost driver",
    description = "Update cost driver",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CostDriverResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<CostDriverResponseDTO> updateCostDriver(
    @Valid @RequestBody CostDriverRequestDTO updateCostDriverRequest,
    @PathVariable(value = "id") String id)
  {
    final var command = new UpdateCostDriverCommand(updateCostDriverRequest, id);
    return commandBus.send(command);
  }

  @PostMapping(value = "cost_drivers")
  @Operation(
    summary = "Add cost driver",
    description = "Add cost driver",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CostDriverResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<CostDriverResponseDTO> addCostDriver(
    @Valid @RequestBody CostDriverRequestDTO addCostDriverRequest)
  {
    final var command = new AddCostDriverCommand(addCostDriverRequest);
    return commandBus.send(command);
  }
}
