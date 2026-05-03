/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

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
import org.springframework.security.access.prepost.PreAuthorize;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.parametrizacoes.application.commands.*;
import cv.igrp.RH_Service.parametrizacoes.application.queries.*;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaOptionDTO;
import java.util.Map;
import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionRequestDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/reference/options")
@Tag(name = "ReferenceOptions", description = "Gestão de catálogos genéricos de etiquetas parametrizáveis")
public class ReferenceOptionsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceOptionsController.class);


  private final CommandBus commandBus;
  private final QueryBus queryBus;


  public ReferenceOptionsController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getOptions",
    description = "GET method to handle operations for getOptions",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListaOptionDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<WrapperListaOptionDTO> getOptions(
    @RequestParam(value = "ccode", required = false) String ccode,
    @RequestParam(value = "locale", required = false) String locale,
    @RequestParam(value = "active", required = false) Boolean active,
    @RequestParam(value = "ckey", required = false) String ckey,
    @RequestParam(value = "pagina", defaultValue = "0") String pagina,
    @RequestParam(value = "tamanho", defaultValue = "20") String tamanho)
  {

      LOGGER.debug("Operation started");

      final var query = new ListOptionsQuery(ccode, locale, active, ckey, pagina, tamanho);

      ResponseEntity<WrapperListaOptionDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{optionId}"
  )
  @Operation(
    summary = "GET method to handle operations for getOptionById",
    description = "GET method to handle operations for getOptionById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = OptionResponseDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<OptionResponseDTO> getOptionById(
    @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetOptionQuery(optionId);

      ResponseEntity<OptionResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createOption",
    description = "POST method to handle operations for createOption",
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

  public ResponseEntity<Map<String, ?>> createOption(@Valid @RequestBody OptionRequestDTO createOptionRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateOptionCommand(createOptionRequest);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{optionId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateOption",
    description = "PUT method to handle operations for updateOption",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = OptionResponseDTO.class,
                  type = "object")
          )
      )
    }
  )

  public ResponseEntity<OptionResponseDTO> updateOption(@Valid @RequestBody OptionRequestDTO updateOptionRequest
    , @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateOptionCommand(updateOptionRequest, optionId);

       ResponseEntity<OptionResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @DeleteMapping(
    value = "{optionId}"
  )
  @Operation(
    summary = "DELETE method to handle operations for deleteOption",
    description = "DELETE method to handle operations for deleteOption",
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

  public ResponseEntity<Map<String, ?>> deleteOption(
    @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new DesativarOptionCommand(optionId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{optionId}/activate"
  )
  @Operation(
    summary = "PATCH method to handle operations for activateOption",
    description = "PATCH method to handle operations for activateOption",
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

  public ResponseEntity<Map<String, ?>> activateOption(
    @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new AtivarOptionCommand(optionId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}
