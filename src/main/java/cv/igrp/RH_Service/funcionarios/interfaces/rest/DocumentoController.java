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


import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/Documento")
@Tag(name = "Documento", description = "GetDocumento")
public class DocumentoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentoController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public DocumentoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getDocumentos",
    description = "GET method to handle operations for getDocumentos",
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
  
  public ResponseEntity<String> getDocumentos(
    @RequestParam(value = "external_id") String external_id,
    @RequestParam(value = "url") String url,
    @RequestParam(value = "observacao") String observacao,
    @RequestParam(value = "object_id") String object_id,
    @RequestParam(value = "estado") String estado)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDocumentosQuery(external_id, url, observacao, object_id, estado);

      ResponseEntity<String> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getDocumentoById",
    description = "GET method to handle operations for getDocumentoById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DocumentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<DocumentoResponseDTO> getDocumentoById(
    @RequestParam(value = "id") String id)
  {

      LOGGER.debug("Operation started");

      final var query = new GetDocumentoByIdQuery(id);

      ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createDocumento",
    description = "POST method to handle operations for createDocumento",
    responses = {
      @ApiResponse(
          responseCode = "201",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = DocumentoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<DocumentoResponseDTO> createDocumento(@Valid @RequestBody DocumentoRequestDTO createDocumentoRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateDocumentoCommand(createDocumentoRequest);

       ResponseEntity<DocumentoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{DocumentoId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateDocumento",
    description = "PUT method to handle operations for updateDocumento",
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
  
  public ResponseEntity<String> updateDocumento(@Valid @RequestBody DocumentoRequestDTO updateDocumentoRequest
    , @PathVariable(value = "DocumentoId") String DocumentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateDocumentoCommand(updateDocumentoRequest, DocumentoId);

       ResponseEntity<String> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{DocumentoId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for inativarDocumento",
    description = "DELETE method to handle operations for inativarDocumento",
    responses = {
      @ApiResponse(
          responseCode = "",
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
  
  public ResponseEntity<Map<String, ?>> inativarDocumento(
    @PathVariable(value = "DocumentoId") String DocumentoId)
  {

      LOGGER.debug("Operation started");

      final var command = new InativarDocumentoCommand(DocumentoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}