/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

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


import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaTipoDocumentoDTO;
import java.util.Map;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoRequestDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/tipoDocumentos")
@Tag(name = "TipoDocumento", description = "Tipo de Documento")
public class TipoDocumentoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TipoDocumentoController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public TipoDocumentoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getTipoDocumento",
    description = "GET method to handle operations for getTipoDocumento",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListaTipoDocumentoDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListaTipoDocumentoDTO> getTipoDocumento(
    @RequestParam(value = "codigo", required = false) String codigo,
    @RequestParam(value = "descricao", required = false) String descricao,
    @RequestParam(value = "estado", required = false) String estado,
    @RequestParam(value = "pagina", defaultValue = "0") String pagina,
    @RequestParam(value = "tamanho", defaultValue = "20") String tamanho)
  {

      LOGGER.debug("Operation started");

      final var query = new GetTipoDocumentoQuery(codigo, descricao, estado, pagina, tamanho);

      ResponseEntity<WrapperListaTipoDocumentoDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{TipoDocumentoId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarTipoDocumento",
    description = "DELETE method to handle operations for inativarTipoDocumento",
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
  
  public ResponseEntity<Map<String, ?>> inativarTipoDocumento(
    @PathVariable(value = "TipoDocumentoId") String TipoDocumentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarTipoDocumentoCommand(TipoDocumentoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{tipoDocumentoId}"
  )
  @Operation(
    summary = "GET method to handle operations for getTipoDocumentoById",
    description = "GET method to handle operations for getTipoDocumentoById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TipoDocumentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<TipoDocumentoResponseDTO> getTipoDocumentoById(
    @PathVariable(value = "tipoDocumentoId") String tipoDocumentoId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetTipoDocumentoByIdQuery(tipoDocumentoId);

      ResponseEntity<TipoDocumentoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{tipoDocumentoId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateTipoDocumento",
    description = "PUT method to handle operations for updateTipoDocumento",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TipoDocumentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<TipoDocumentoResponseDTO> updateTipoDocumento(@Valid @RequestBody TipoDocumentoRequestDTO updateTipoDocumentoRequest
    , @PathVariable(value = "tipoDocumentoId") String tipoDocumentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateTipoDocumentoCommand(updateTipoDocumentoRequest, tipoDocumentoId);

       ResponseEntity<TipoDocumentoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createTipoDocumento",
    description = "POST method to handle operations for createTipoDocumento",
    responses = {
      @ApiResponse(
          responseCode = "201",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = TipoDocumentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<TipoDocumentoResponseDTO> createTipoDocumento(@Valid @RequestBody TipoDocumentoRequestDTO createTipoDocumentoRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateTipoDocumentoCommand(createTipoDocumentoRequest);

       ResponseEntity<TipoDocumentoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}