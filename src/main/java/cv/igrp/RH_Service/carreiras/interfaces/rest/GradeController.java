/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.carreiras.interfaces.rest;

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
import cv.igrp.RH_Service.carreiras.application.commands.*;
import cv.igrp.RH_Service.carreiras.application.queries.*;
import cv.igrp.RH_Service.carreiras.application.dto.WrapperListaGradeDTO;
import cv.igrp.RH_Service.carreiras.application.dto.GradeResponseDTO;
import cv.igrp.RH_Service.carreiras.application.dto.GradeRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/grades")
@Tag(name = "Grade", description = "Gestão de escalões salariais (PCFR)")
public class GradeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradeController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public GradeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar escalões",
        description = "Retorna a lista paginada de escalões, com filtro opcional por categoryId",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaGradeDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaGradeDTO> getGrades(
        @RequestParam(value = "categoryId", required = false) String categoryId,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");

        final var query = new GetGradesQuery(categoryId, active, pagina, tamanho, code, nome);
        ResponseEntity<WrapperListaGradeDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{gradeId}")
    @Operation(
        summary = "Obter escalão por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GradeResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<GradeResponseDTO> getGradeById(
        @PathVariable(value = "gradeId") String gradeId) {

        LOGGER.debug("Operation started");

        final var query = new GetGradeByIdQuery(gradeId);
        ResponseEntity<GradeResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar escalão",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createGrade(
        @Valid @RequestBody GradeRequestDTO createGradeRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateGradeCommand(createGradeRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{gradeId}")
    @Operation(
        summary = "Actualizar escalão",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GradeResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<GradeResponseDTO> updateGrade(
        @Valid @RequestBody GradeRequestDTO updateGradeRequest,
        @PathVariable(value = "gradeId") String gradeId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateGradeCommand(updateGradeRequest, gradeId);
        ResponseEntity<GradeResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{gradeId}")
    @Operation(
        summary = "Desactivar escalão (soft delete)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> desativarGrade(
        @PathVariable(value = "gradeId") String gradeId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarGradeCommand(gradeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{gradeId}/activate")
    @Operation(
        summary = "Reactivar escalão",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateGrade(
        @PathVariable(value = "gradeId") String gradeId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarGradeCommand(gradeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("combobox")
    @Operation(
        summary = "Listar para combobox",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<List<ComboboxItemDTO>> getCombobox(
        @RequestParam(value = "categoryId", required = false) String categoryId) {
        LOGGER.debug("Operation started");
        final var query = new GetGradesComboboxQuery(categoryId);
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}