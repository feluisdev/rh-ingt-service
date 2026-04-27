/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCreateCostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateDelegationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateInstitutionRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.DeactivateInstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.DelegationResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/admin")
@Tag(
    name = "Admin",
    description = "Administration: institutions, delegations, SIADAP configuration and cost drivers"
)
public class AdminController {

  private final QueryBus queryBus;
  private final CommandBus commandBus;

  public AdminController(QueryBus queryBus, CommandBus commandBus) {
    this.queryBus = queryBus;
    this.commandBus = commandBus;
  }

  @PostMapping(value = "institutions")
  @Operation(
    summary = "Create institution",
    description = "Cria uma nova instituição no sistema.",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = InstitutionResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<InstitutionResponseDTO> createInstitution(
    @Valid @RequestBody CreateInstitutionRequestDTO body)
  {
    final var command = new CreateInstitutionCommand(body);
    return commandBus.send(command);
  }

  @DeleteMapping(value = "institutions/{id}")
  @Operation(
    summary = "Deactivate institution",
    description = "Desativa uma instituição existente.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DeactivateInstitutionResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<DeactivateInstitutionResponseDTO> deactivateInstitution(
    @PathVariable(value = "id") String id)
  {
    final var command = new DeactivateInstitutionCommand(id);
    return commandBus.send(command);
  }

  @GetMapping(value = "institutions/{id}")
  @Operation(
    summary = "Get institution by ID",
    description = "Obtém uma instituição pelo seu identificador único.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = InstitutionResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<InstitutionResponseDTO> getInstitutionById(
    @PathVariable(value = "id") String id)
  {
    final var query = new GetInstitutionByIdQuery(id);
    return queryBus.handle(query);
  }

  @PostMapping(value = "delegations")
  @Operation(
    summary = "Create delegation",
    description = "Cria uma delegação de competências entre utilizadores.",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DelegationResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<DelegationResponseDTO> createDelegation(
    @RequestParam(value = "delegatorUserId", required = false) String delegatorUserId,
    @Valid @RequestBody CreateDelegationRequestDTO body)
  {
    final var command = new CreateDelegationCommand(delegatorUserId, body);
    return commandBus.send(command);
  }

  @PutMapping(value = "siadap-config/{year}")
  @Operation(
    summary = "Upsert SIADAP configuration",
    description = "Cria ou actualiza a configuração SIADAP para o ano indicado.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapConfigResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapConfigResponseDTO> upsertSiadapConfig(
    @PathVariable(value = "year") Integer year,
    @Valid @RequestBody SiadapConfigRequestDTO body)
  {
    final var command = new UpsertSiadapConfigCommand(year, body);
    return commandBus.send(command);
  }

  @GetMapping(value = "siadap-config/{year}")
  @Operation(
    summary = "Get SIADAP configuration",
    description = "Obtém a configuração SIADAP para o ano indicado. Se não existir, recorre ao ano anterior.",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SiadapConfigResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<SiadapConfigResponseDTO> getSiadapConfig(
    @PathVariable(value = "year") Integer year)
  {
    final var query = new GetSiadapConfigQuery(year);
    return queryBus.handle(query);
  }

  @PostMapping(value = "cost-drivers")
  @Operation(
    summary = "Create cost driver",
    description = "Regista um novo driver de custo (ajudas de custo, combustível, etc.).",
    responses = {
      @ApiResponse(
          responseCode = "201",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AdminCostDriverResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<AdminCostDriverResponseDTO> createCostDriver(
    @Valid @RequestBody AdminCreateCostDriverRequestDTO body)
  {
    final var command = new AdminCreateCostDriverCommand(body);
    return commandBus.send(command);
  }
}
