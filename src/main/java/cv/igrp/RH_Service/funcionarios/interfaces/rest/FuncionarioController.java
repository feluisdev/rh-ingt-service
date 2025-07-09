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


import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaFuncionarioDTO;
import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/funcionarios")
@Tag(name = "Funcionario", description = "gestao funcionarios")
public class FuncionarioController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FuncionarioController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public FuncionarioController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createFuncionario",
    description = "POST method to handle operations for createFuncionario",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = FuncionarioResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<FuncionarioResponseDTO> createFuncionario(@Valid @RequestBody FuncionarioRequestDTO createFuncionarioRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateFuncionarioCommand(createFuncionarioRequest);

       ResponseEntity<FuncionarioResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getFuncionarios",
    description = "GET method to handle operations for getFuncionarios",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListaFuncionarioDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListaFuncionarioDTO> getFuncionarios(
    @RequestParam(value = "nome", required = false) String nome,
    @RequestParam(value = "email", required = false) String email,
    @RequestParam(value = "numSegurado", required = false) String numSegurado,
    @RequestParam(value = "nif", required = false) String nif,
    @RequestParam(value = "pagina", required = false, defaultValue = "0") String pagina,
    @RequestParam(value = "tamanho", required = false, defaultValue = "20") String tamanho)
  {

      LOGGER.debug("Operation started");

      final var query = new GetFuncionariosQuery(nome, email, numSegurado, nif, pagina, tamanho);

      ResponseEntity<WrapperListaFuncionarioDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{funcionarioId}"
  )
  @Operation(
    summary = "GET method to handle operations for getFuncionarioById",
    description = "GET method to handle operations for getFuncionarioById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = FuncionarioResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<FuncionarioResponseDTO> getFuncionarioById(
    @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetFuncionarioByIdQuery(funcionarioId);

      ResponseEntity<FuncionarioResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{funcionarioId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateFuncionario",
    description = "PUT method to handle operations for updateFuncionario",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = FuncionarioResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<FuncionarioResponseDTO> updateFuncionario(@Valid @RequestBody FuncionarioRequestDTO updateFuncionarioRequest
    , @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateFuncionarioCommand(updateFuncionarioRequest, funcionarioId);

       ResponseEntity<FuncionarioResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{funcionarioId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarFuncionario",
    description = "DELETE method to handle operations for inativarFuncionario",
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
  
  public ResponseEntity<Map<String, ?>> inativarFuncionario(
    @PathVariable(value = "funcionarioId") String funcionarioId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarFuncionarioCommand(funcionarioId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}