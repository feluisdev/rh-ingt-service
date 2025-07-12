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


import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaCargoDTO;
import java.util.Map;
import cv.igrp.RH_Service.funcionarios.application.dto.CargoRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/cargos")
@Tag(name = "Cargo", description = "gestao de cargos")
public class CargoController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CargoController.class);

  
  private final CommandBus commandBus;
  private final QueryBus queryBus;

  
  public CargoController(
    CommandBus commandBus, QueryBus queryBus
  ) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @GetMapping(
  )
  @Operation(
    summary = "GET method to handle operations for getCargos",
    description = "GET method to handle operations for getCargos",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = WrapperListaCargoDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<WrapperListaCargoDTO> getCargos(
    @RequestParam(value = "nome", required = false) String nome,
    @RequestParam(value = "codigo", required = false) String codigo,
    @RequestParam(value = "nivelHierarquico", required = false) Integer nivelHierarquico,
    @RequestParam(value = "salarioBaseMax", required = false) Integer salarioBaseMax,
    @RequestParam(value = "salarioBaseMin", required = false) Integer salarioBaseMin,
    @RequestParam(value = "estado", required = false) String estado,
    @RequestParam(value = "pagina", defaultValue = "0") String pagina,
    @RequestParam(value = "tamanho", defaultValue = "20") String tamanho)
  {

      LOGGER.debug("Operation started");

      final var query = new GetCargosQuery(nome, codigo, nivelHierarquico, salarioBaseMax, salarioBaseMin, estado, pagina, tamanho);

      ResponseEntity<WrapperListaCargoDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{cargoId}/ativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for ativarCargo",
    description = "PATCH method to handle operations for ativarCargo",
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
  
  public ResponseEntity<Map<String, ?>> ativarCargo(
    @PathVariable(value = "cargoId") String cargoId)
  {

      LOGGER.debug("Operation started");

      final var command = new AtivarCargoCommand(cargoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PatchMapping(
    value = "{cargoId}/desativar"
  )
  @Operation(
    summary = "PATCH method to handle operations for desativarCargo",
    description = "PATCH method to handle operations for desativarCargo",
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
  
  public ResponseEntity<Map<String, ?>> desativarCargo(
    @PathVariable(value = "cargoId") String cargoId)
  {

      LOGGER.debug("Operation started");

      final var command = new DesativarCargoCommand(cargoId);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @GetMapping(
    value = "{cargoId}"
  )
  @Operation(
    summary = "GET method to handle operations for getCargoById",
    description = "GET method to handle operations for getCargoById",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CargoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CargoResponseDTO> getCargoById(@Valid @RequestBody CargoRequestDTO getCargoByIdRequest
    , @PathVariable(value = "cargoId") String cargoId)
  {

      LOGGER.debug("Operation started");

      final var query = new GetCargoByIdQuery(getCargoByIdRequest, cargoId);

      ResponseEntity<CargoResponseDTO> response = queryBus.handle(query);

      LOGGER.debug("Operation finished");

      return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PostMapping(
  )
  @Operation(
    summary = "POST method to handle operations for createCargo",
    description = "POST method to handle operations for createCargo",
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
  
  public ResponseEntity<Map<String, ?>> createCargo(@Valid @RequestBody CargoRequestDTO createCargoRequest
    )
  {

      LOGGER.debug("Operation started");

      final var command = new CreateCargoCommand(createCargoRequest);

       ResponseEntity<Map<String, ?>> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

  @PutMapping(
    value = "{cargoId}"
  )
  @Operation(
    summary = "PUT method to handle operations for updateCargo",
    description = "PUT method to handle operations for updateCargo",
    responses = {
      @ApiResponse(
          responseCode = "200",
          description = "",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = CargoResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<CargoResponseDTO> updateCargo(@Valid @RequestBody CargoRequestDTO updateCargoRequest
    , @PathVariable(value = "cargoId") String cargoId)
  {

      LOGGER.debug("Operation started");

      final var command = new UpdateCargoCommand(updateCargoRequest, cargoId);

       ResponseEntity<CargoResponseDTO> response = commandBus.send(command);

       LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
              .headers(response.getHeaders())
              .body(response.getBody());
  }

}