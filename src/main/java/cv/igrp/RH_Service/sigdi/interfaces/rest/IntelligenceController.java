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
import cv.igrp.RH_Service.sigdi.application.dto.CreateScenarioRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ScenarioResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperScenarioListDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/intelligence")
@Tag(
    name = "SIGDI-Intelligence",
    description = "Scenario simulation and impact analysis"
)
public class IntelligenceController {

  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public IntelligenceController(QueryBus queryBus, CommandBus commandBus) {
    this.queryBus = queryBus;
    this.commandBus = commandBus;
  }

  @PostMapping(value = "scenarios")
  @Operation(
    summary = "Create simulation scenario",
    description = "Inicia uma simulação de cenário orçamental. Síncrona para < 100 atividades, assíncrona para ≥ 100.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ScenarioResponseDTO.class, type = "object")
          )
      ),
      @ApiResponse(
          responseCode = "202",
          description = "Async processing started"
      )
    }
  )
  public ResponseEntity<?> createScenario(
    @Valid @RequestBody CreateScenarioRequestDTO createScenarioRequest)
  {
    final var command = new CreateScenarioCommand(createScenarioRequest);
    return commandBus.send(command);
  }

  @GetMapping(value = "scenarios/{id}")
  @Operation(
    summary = "Get scenario",
    description = "Consulta estado e resultado de um cenário.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ScenarioResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<ScenarioResponseDTO> getScenario(
    @PathVariable(value = "id") String id)
  {
    final var query = new GetScenarioQuery(id);
    return queryBus.handle(query);
  }

  @GetMapping(value = "scenarios/{id}/export")
  @Operation(
    summary = "Export scenario report",
    description = "Exporta o relatório de impacto do cenário em PDF ou EXCEL.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "Binary file content"
      )
    }
  )
  public ResponseEntity<byte[]> exportScenario(
    @PathVariable(value = "id") String id,
    @RequestParam(value = "format", required = false, defaultValue = "PDF") String format)
  {
    final var query = new GetScenarioExportQuery(id, format);
    return queryBus.handle(query);
  }

  @GetMapping(value = "scenarios")
  @Operation(
    summary = "List scenarios",
    description = "Lista cenários guardados da instituição.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = WrapperScenarioListDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<WrapperScenarioListDTO> listScenarios(
    @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize",   required = false, defaultValue = "20") String pageSize)
  {
    final var query = new ListScenariosQuery(pageNumber, pageSize);
    return queryBus.handle(query);
  }
}
