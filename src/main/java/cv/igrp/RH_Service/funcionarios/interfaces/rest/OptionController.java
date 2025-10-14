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
import org.springframework.security.access.prepost.PreAuthorize;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.funcionarios.application.commands.*;
import cv.igrp.RH_Service.funcionarios.application.queries.*;

import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaOptionDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionRequestDTO;
import java.util.Map;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionCodeResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/options")
@Tag(name = "Option", description = "gestao de opcoes")
public class OptionController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public OptionController(
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
    @RequestParam(value = "ckey", required = false) String ckey,
    @RequestParam(value = "cvalue", required = false) String cvalue,
    @RequestParam(value = "locale", required = false) String locale,
    @RequestParam(value = "sort_order", required = false) Integer sort_order,
    @RequestParam(value = "active", required = false) String active,
    @RequestParam(value = "description", required = false) String description,
    @RequestParam(value = "pageNumber", defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", defaultValue = "20") String pageSize)
  {

      LOGGER.debug("Operation started");

      final var query = new GetOptionsQuery(ccode, ckey, cvalue, locale, sort_order, active, description, pageNumber, pageSize);

      ResponseEntity<WrapperListaOptionDTO> response = queryBus.handle(query);

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
                  implementation = String.class,
                  type = "String")
          )
      )
    }
  )
  
  public ResponseEntity<String> updateOption(@Valid @RequestBody OptionRequestDTO updateOptionRequest
    , @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateOptionCommand(updateOptionRequest, optionId);

       ResponseEntity<String> response = commandBus.send(command);

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

      final var query = new GetOptionByIdQuery(optionId);

      ResponseEntity<OptionResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{optionId}/ativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for ativarOption",
    description = "PATCH method to handle operations for ativarOption",
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
  
  public ResponseEntity<Map<String, ?>> ativarOption(
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

  @PatchMapping(
    value = "{optionId}/desativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for desativarOption",
    description = "PATCH method to handle operations for desativarOption",
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
  
  public ResponseEntity<Map<String, ?>> desativarOption(
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

  @GetMapping(
    value = "{ccode}/findByCcode"
  )
  @Operation(
    summary = "GET method to handle operations for findByCcode",
    description = "GET method to handle operations for findByCcode",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = OptionCodeResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<OptionCodeResponseDTO> findByCcode(
    @RequestParam(value = "locale") String locale,
    @RequestParam(value = "includeInactive", defaultValue = "1") String includeInactive,
    @RequestParam(value = "format", defaultValue = "list") String format, @PathVariable(value = "ccode") String ccode)
  {

      LOGGER.debug("Operation started");

      final var query = new FindByCcodeQuery(locale, includeInactive, format, ccode);

      ResponseEntity<OptionCodeResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}