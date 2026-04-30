/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.parametrizacoes.application.commands.*;
import cv.igrp.RH_Service.parametrizacoes.application.queries.*;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaProfessionalSituationDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/professional-situations")
@Tag(name = "ProfessionalSituation", description = "Gestão de situações profissionais")
public class ProfessionalSituationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfessionalSituationController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ProfessionalSituationController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar situações profissionais",
        description = "Retorna a lista paginada de situações profissionais",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaProfessionalSituationDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaProfessionalSituationDTO> listProfessionalSituations(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new ListProfessionalSituationsQuery(code, isActive, pagina, tamanho);
        ResponseEntity<WrapperListaProfessionalSituationDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{professionalSituationId}")
    @Operation(
        summary = "Obter situação profissional por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProfessionalSituationResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ProfessionalSituationResponseDTO> getProfessionalSituationById(
        @PathVariable(value = "professionalSituationId") String professionalSituationId) {

        LOGGER.debug("Operation started");

        final var query = new GetProfessionalSituationQuery(professionalSituationId);
        ResponseEntity<ProfessionalSituationResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar situação profissional",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createProfessionalSituation(
        @Valid @RequestBody ProfessionalSituationRequestDTO createProfessionalSituationRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateProfessionalSituationCommand(createProfessionalSituationRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{professionalSituationId}")
    @Operation(
        summary = "Atualizar situação profissional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProfessionalSituationResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ProfessionalSituationResponseDTO> updateProfessionalSituation(
        @Valid @RequestBody ProfessionalSituationRequestDTO updateProfessionalSituationRequest,
        @PathVariable(value = "professionalSituationId") String professionalSituationId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateProfessionalSituationCommand(updateProfessionalSituationRequest, professionalSituationId);
        ResponseEntity<ProfessionalSituationResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{professionalSituationId}")
    @Operation(
        summary = "Desativar situação profissional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteProfessionalSituation(
        @PathVariable(value = "professionalSituationId") String professionalSituationId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarProfessionalSituationCommand(professionalSituationId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{professionalSituationId}/activate")
    @Operation(
        summary = "Ativar situação profissional",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateProfessionalSituation(
        @PathVariable(value = "professionalSituationId") String professionalSituationId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarProfessionalSituationCommand(professionalSituationId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
