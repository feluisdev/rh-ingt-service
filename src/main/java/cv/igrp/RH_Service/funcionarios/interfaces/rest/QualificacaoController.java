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


import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoRequestDTO;
import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/funcionarios")
@Tag(name = "Qualificacao", description = "gestao Qualificacoes")
public class QualificacaoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(QualificacaoController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public QualificacaoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @PostMapping(
    value = "{funcionarioId}/qualificacoes"
  )
  @Operation(
    summary = "POST method to handle operations for createQualificacao",
    description = "POST method to handle operations for createQualificacao",
    responses = {
      @ApiResponse(
          responseCode = "201",
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
  
  public ResponseEntity<Map<String, ?>> createQualificacao(@Valid @RequestBody QualificacaoRequestDTO createQualificacaoRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var command = new CreateQualificacaoCommand(createQualificacaoRequest, funcionarioId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}/qualificacoes"
  )
  @Operation(
    summary = "GET method to handle operations for getQualificacoesFuncionarios",
    description = "GET method to handle operations for getQualificacoesFuncionarios",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = QualificacaoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<List<QualificacaoResponseDTO>> getQualificacoesFuncionarios(
    @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetQualificacoesFuncionariosQuery(funcionarioId);

      ResponseEntity<List<QualificacaoResponseDTO>> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}/qualificacoes/{qualificacaoId}"
  )
  @Operation(
    summary = "GET method to handle operations for getQualificacao",
    description = "GET method to handle operations for getQualificacao",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = QualificacaoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<QualificacaoResponseDTO> getQualificacao(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "qualificacaoId") String qualificacaoId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetQualificacaoQuery(funcionarioId, qualificacaoId);

      ResponseEntity<QualificacaoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{funcionarioId}/qualificacoes/{qualificacaoId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateQualificacao",
    description = "PUT method to handle operations for updateQualificacao",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = QualificacaoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<QualificacaoResponseDTO> updateQualificacao(@Valid @RequestBody QualificacaoRequestDTO updateQualificacaoRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "qualificacaoId") String qualificacaoId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateQualificacaoCommand(updateQualificacaoRequest, funcionarioId, qualificacaoId);

       ResponseEntity<QualificacaoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{funcionarioId}/qualificacoes/{qualificacaoId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarQualificacao",
    description = "DELETE method to handle operations for inativarQualificacao",
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
  
  public ResponseEntity<Map<String, ?>> inativarQualificacao(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "qualificacaoId") String qualificacaoId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarQualificacaoCommand(funcionarioId, qualificacaoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{funcionarioId}/qualificacoes/{qualificacaoId}"
  )
  @Operation(
    summary = "PATCH method to handle operations for ativarQualificacao",
    description = "PATCH method to handle operations for ativarQualificacao",
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
  
  public ResponseEntity<Map<String, ?>> ativarQualificacao(
    @PathVariable(value = "funcionarioId") String funcionarioId,@PathVariable(value = "qualificacaoId") String qualificacaoId)
  {

      LOGGER.debug("Operation started");

      final var command = new AtivarQualificacaoCommand(funcionarioId, qualificacaoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}