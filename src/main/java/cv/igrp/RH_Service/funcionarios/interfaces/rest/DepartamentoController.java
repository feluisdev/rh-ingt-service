/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
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


import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaDepartamentoDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoResponseDTO;
import java.util.Map;
import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoRequestDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/departamentos")
@Tag(name = "Departamento", description = "gestao departamentos")
public class DepartamentoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DepartamentoController.class);


  private final CommandBus commandBus;
  private final QueryBus queryBus;


  public DepartamentoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getDepartamentos",
    description = "GET method to handle operations for getDepartamentos",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListaDepartamentoDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<WrapperListaDepartamentoDTO> getDepartamentos(
    @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
    @RequestParam(value = "pagina", defaultValue = "0") String pagina,
    @RequestParam(value = "nome", required = false) String nome,
    @RequestParam(value = "localizacao", required = false) String localizacao,
    @RequestParam(value = "codigo", required = false) String codigo,
    @RequestParam(value = "responsavelId", required = false) String responsavelId,
    @RequestParam(value = "estado", required = false) String estado)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDepartamentosQuery(tamanho, pagina, nome, localizacao, codigo, responsavelId, estado);

      ResponseEntity<WrapperListaDepartamentoDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{departamentoId}"
  )
  @Operation(
    summary = "GET method to handle operations for getDepartamentoById",
    description = "GET method to handle operations for getDepartamentoById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DepartamentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<DepartamentoResponseDTO> getDepartamentoById(
    @PathVariable(value = "departamentoId") String departamentoId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDepartamentoByIdQuery(departamentoId);

      ResponseEntity<DepartamentoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{departamentoId}/ativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for ativarDepartamento",
    description = "PATCH method to handle operations for ativarDepartamento",
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

  public ResponseEntity<Map<String, ?>> ativarDepartamento(
    @PathVariable(value = "departamentoId") String departamentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new AtivarDepartamentoCommand(departamentoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{departamentoId}/desativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for desativarDepartamento",
    description = "PATCH method to handle operations for desativarDepartamento",
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

  public ResponseEntity<Map<String, ?>> desativarDepartamento(
    @PathVariable(value = "departamentoId") String departamentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new DesativarDepartamentoCommand(departamentoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createDepartamento",
    description = "POST method to handle operations for createDepartamento",
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

  public ResponseEntity<Map<String, ?>> createDepartamento(@Valid @RequestBody DepartamentoRequestDTO createDepartamentoRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateDepartamentoCommand(createDepartamentoRequest);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{departamentoId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateDepartamento",
    description = "PUT method to handle operations for updateDepartamento",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DepartamentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<DepartamentoResponseDTO> updateDepartamento(@Valid @RequestBody DepartamentoRequestDTO updateDepartamentoRequest
    , @PathVariable(value = "departamentoId") String departamentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateDepartamentoCommand(updateDepartamentoRequest, departamentoId);

       ResponseEntity<DepartamentoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}
