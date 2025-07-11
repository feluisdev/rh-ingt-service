package cv.igrp.RH_Service.contratos.infrastructure.controller;

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
import cv.igrp.RH_Service.contratos.application.commands.commands.*;
import cv.igrp.RH_Service.contratos.application.queries.queries.*;


import cv.igrp.RH_Service.contratos.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.contratos.application.dto.WrapperListaContratoDTO;
import cv.igrp.RH_Service.contratos.application.dto.ContratoRequestDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/contratos")
@Tag(name = "Contrato", description = "Gestão de Contratos")
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
  )
  @Operation(
    summary = "GET method to handle operations for getContratoById",
    description = "GET method to handle operations for getContratoById",
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
  
  public ResponseEntity<ContratoResponseDTO> getContratoById(
    @RequestParam(value = "contratoId") Integer contratoId)
  {
      LOGGER.debug("Operation started - Endpoint: {}, Action: {}", "ContratoController", "getContratoById");
      final var query = new GetContratoByIdQuery(contratoId);
      ResponseEntity<ContratoResponseDTO> response = queryBus.handle(query);
      LOGGER.debug("Operation finished - Endpoint: {}, Action: {}", "ContratoController", "getContratoById");
      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
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
                  implementation = WrapperListaContratoDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListaContratoDTO> getContrato(
    @RequestParam(value = "tipo_contrato") String tipo_contrato)
  {
      LOGGER.debug("Operation started - Endpoint: {}, Action: {}", "ContratoController", "getContrato");
      final var query = new GetContratoQuery(tipo_contrato);
      ResponseEntity<WrapperListaContratoDTO> response = queryBus.handle(query);
      LOGGER.debug("Operation finished - Endpoint: {}, Action: {}", "ContratoController", "getContrato");
      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
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
  
  public ResponseEntity<String> createContrato(@Valid @RequestBody ContratoRequestDTO createContratoRequest
    )
  {
      LOGGER.debug("Operation started - Endpoint: {}, Action: {}", "ContratoController", "createContrato");
      final var command = new CreateContratoCommand(createContratoRequest);
       ResponseEntity<String> response = commandBus.send(command);
       LOGGER.debug("Operation finished - Endpoint: {}, Action: {}", "ContratoController", "createContrato");
        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
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
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<String> updateContrato(@Valid @RequestBody ContratoRequestDTO updateContratoRequest
    )
  {
      LOGGER.debug("Operation started - Endpoint: {}, Action: {}", "ContratoController", "updateContrato");
      final var command = new UpdateContratoCommand(updateContratoRequest);
       ResponseEntity<String> response = commandBus.send(command);
       LOGGER.debug("Operation finished - Endpoint: {}, Action: {}", "ContratoController", "updateContrato");
        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarContrato",
    description = "DELETE method to handle operations for inativarContrato",
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
  
  public ResponseEntity<String> inativarContrato(
    @RequestParam(value = "contratoId") String contratoId)
  {
      LOGGER.debug("Operation started - Endpoint: {}, Action: {}", "ContratoController", "inativarContrato");
      final var command = new InativarContratoCommand(contratoId);
       ResponseEntity<String> response = commandBus.send(command);
       LOGGER.debug("Operation finished - Endpoint: {}, Action: {}", "ContratoController", "inativarContrato");
        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}