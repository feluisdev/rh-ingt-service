package cv.igrp.RH_Service.funcionarios.interfaces.rest;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.funcionarios.application.commands.*;
import cv.igrp.RH_Service.funcionarios.application.queries.*;


import cv.igrp.RH_Service.funcionarios.application.dto.DependenteRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;
import java.util.List;
import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/funcionarios")
@Tag(name = "Dependente", description = "gestao de dependentes")
public class DependenteController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DependenteController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public DependenteController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @PostMapping(
    value = "{funcionarioId}/dependentes"
  )
  @Operation(
    summary = "POST method to handle operations for createDependente",
    description = "POST method to handle operations for createDependente",
    responses = {
      @ApiResponse(
          responseCode = "201",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DependenteResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<DependenteResponseDTO> createDependente(@Valid @RequestBody DependenteRequestDTO createDependenteRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var command = new CreateDependenteCommand(createDependenteRequest, funcionarioId);

       ResponseEntity<DependenteResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}/dependentes"
  )
  @Operation(
    summary = "GET method to handle operations for getDepentesByFuncionario",
    description = "GET method to handle operations for getDepentesByFuncionario",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DependenteResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<List<DependenteResponseDTO>> getDepentesByFuncionario(
    @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDepentesByFuncionarioQuery(funcionarioId);

      ResponseEntity<List<DependenteResponseDTO>> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}/dependentes/{dependenteId}"
  )
  @Operation(
    summary = "GET method to handle operations for getDependenteById",
    description = "GET method to handle operations for getDependenteById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DependenteResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<DependenteResponseDTO> getDependenteById(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "dependenteId") String dependenteId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDependenteByIdQuery(funcionarioId, dependenteId);

      ResponseEntity<DependenteResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{funcionarioId}/dependentes/{dependenteId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateDependente",
    description = "PUT method to handle operations for updateDependente",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DependenteResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<DependenteResponseDTO> updateDependente(@Valid @RequestBody DependenteRequestDTO updateDependenteRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "dependenteId") String dependenteId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateDependenteCommand(updateDependenteRequest, funcionarioId, dependenteId);

       ResponseEntity<DependenteResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{funcionarioId}/dependentes/{dependenteId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarDependente",
    description = "DELETE method to handle operations for inativarDependente",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<Map<String, ?>> inativarDependente(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "dependenteId") String dependenteId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarDependenteCommand(funcionarioId, dependenteId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}