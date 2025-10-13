/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.options.interfaces.rest;

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
import cv.igrp.RH_Service.options.application.commands.*;
import cv.igrp.RH_Service.options.application.queries.*;

import cv.igrp.RH_Service.options.application.dto.WrapperListOptionsDTO;
import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.options.application.dto.OptionRequestDTO;
import java.util.Map;
import cv.igrp.RH_Service.options.application.dto.OptionCodeResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/options")
@Tag(name = "Options", description = "Gestão de opções")
public class OptionsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionsController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public OptionsController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getListOptions",
    description = "GET method to handle operations for getListOptions",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListOptionsDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListOptionsDTO> getListOptions(
    @RequestParam(value = "ccode", required = false) String ccode,
    @RequestParam(value = "ckey", required = false) String ckey,
    @RequestParam(value = "cvalue", required = false) String cvalue,
    @RequestParam(value = "locale", required = false) String locale,
    @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
    @RequestParam(value = "active", required = false, defaultValue = "true") boolean active,
    @RequestParam(value = "pageNumber", defaultValue = "0") String pageNumber,
    @RequestParam(value = "pageSize", defaultValue = "20") String pageSize)
  {

      LOGGER.debug("Operation started");

      final var query = new GetListOptionsQuery(ccode, ckey, cvalue, locale, sortOrder, active, pageNumber, pageSize);

      ResponseEntity<WrapperListOptionsDTO> response = queryBus.handle(query);

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

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createOptions",
    description = "POST method to handle operations for createOptions",
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
  
  public ResponseEntity<OptionResponseDTO> createOptions(@Valid @RequestBody OptionRequestDTO createOptionsRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateOptionsCommand(createOptionsRequest);

       ResponseEntity<OptionResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{optionId}/disable"
  )
  @Operation(
    summary = "PATCH method to handle operations for disableOption",
    description = "PATCH method to handle operations for disableOption",
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
  
  public ResponseEntity<Map<String, ?>> disableOption(
    @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new DisableOptionCommand(optionId);

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

  @PatchMapping(
    value = "{optionId}/enable"
  )
  @Operation(
    summary = "PATCH method to handle operations for enableOption",
    description = "PATCH method to handle operations for enableOption",
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
  
  public ResponseEntity<Map<String, ?>> enableOption(
    @PathVariable(value = "optionId") String optionId)
  {

      LOGGER.debug("Operation started");

      final var command = new EnableOptionCommand(optionId);

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
    @RequestParam(value = "locale", required = false) String locale,
    @RequestParam(value = "includeInactive", required = false, defaultValue = "1") boolean includeInactive,
    @RequestParam(value = "format", required = false, defaultValue = "list") String format, @PathVariable(value = "ccode") String ccode)
  {

      LOGGER.debug("Operation started");

      final var query = new FindByCcodeQuery(locale, includeInactive, format, ccode);

      ResponseEntity<OptionCodeResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{ccode}/{ckey}/exists"
  )
  @Operation(
    summary = "GET method to handle operations for existsByCcodeAndCkey",
    description = "GET method to handle operations for existsByCcodeAndCkey",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = boolean.class,
                  type = "boolean")
          )
      )
    }
  )
  
  public ResponseEntity<Boolean> existsByCcodeAndCkey(
    @PathVariable(value = "ccode") String ccode,@PathVariable(value = "ckey") String ckey)
  {

      LOGGER.debug("Operation started");

      final var query = new ExistsByCcodeAndCkeyQuery(ccode, ckey);

      ResponseEntity<Boolean> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}