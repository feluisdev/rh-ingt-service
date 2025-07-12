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


import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;
import java.util.List;
import cv.igrp.RH_Service.funcionarios.application.dto.ContratoRequestDTO;
import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/funcionarios")
@Tag(name = "Contrato", description = "Gestão de Contrato")
public class ContratoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public ContratoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
    value = "{funcionarioId}/contratos/{contratoId}"
  )
  @Operation(
    summary = "GET method to handle operations for getContrato",
    description = "GET method to handle operations for getContrato",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = ContratoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<ContratoResponseDTO> getContrato(
    @RequestParam(value = "id") String id, @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "contratoId") String contratoId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetContratoQuery(id, funcionarioId, contratoId);

      ResponseEntity<ContratoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}/contratos"
  )
  @Operation(
    summary = "GET method to handle operations for getContratos",
    description = "GET method to handle operations for getContratos",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = ContratoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<List<ContratoResponseDTO>> getContratos(
    @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetContratosQuery(funcionarioId);

      ResponseEntity<List<ContratoResponseDTO>> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
    value = "{funcionarioId}/contratos"
  )
  @Operation(
    summary = "POST method to handle operations for createContrato",
    description = "POST method to handle operations for createContrato",
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
  
  public ResponseEntity<Map<String, ?>> createContrato(@Valid @RequestBody ContratoRequestDTO createContratoRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var command = new CreateContratoCommand(createContratoRequest, funcionarioId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{funcionarioId}/contratos"
  )
  @Operation(
    summary = "PUT method to handle operations for updateContrato",
    description = "PUT method to handle operations for updateContrato",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = ContratoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<ContratoResponseDTO> updateContrato(@Valid @RequestBody ContratoRequestDTO updateContratoRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateContratoCommand(updateContratoRequest);

       ResponseEntity<ContratoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{funcionarioId}/contratos/{contratoId}"
  )
  @Operation(
    summary = "PATCH method to handle operations for inativarContrato",
    description = "PATCH method to handle operations for inativarContrato",
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
  
  public ResponseEntity<Map<String, ?>> inativarContrato(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "contratoId") String contratoId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarContratoCommand(funcionarioId, contratoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{funcionarioId}/contratos/{contratoId}"
  )
  @Operation(
    summary = "PATCH method to handle operations for ativarContrato",
    description = "PATCH method to handle operations for ativarContrato",
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
  
  public ResponseEntity<Map<String, ?>> ativarContrato(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "contratoId") String contratoId)
  {

      LOGGER.debug("Operation started");

      final var command = new AtivarContratoCommand(funcionarioId, contratoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}