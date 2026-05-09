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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaWorkerStateDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WorkerStateResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WorkerStateRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/worker-states")
@Tag(name = "WorkerState", description = "Gestão de estados do trabalhador")
public class WorkerStateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerStateController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public WorkerStateController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar estados do trabalhador",
        description = "Retorna a lista paginada de estados do trabalhador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaWorkerStateDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaWorkerStateDTO> listWorkerStates(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new ListWorkerStatesQuery(code, isActive, pagina, tamanho);
        ResponseEntity<WrapperListaWorkerStateDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{workerStateId}")
    @Operation(
        summary = "Obter estado do trabalhador por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WorkerStateResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WorkerStateResponseDTO> getWorkerStateById(
        @PathVariable(value = "workerStateId") String workerStateId) {

        LOGGER.debug("Operation started");

        final var query = new GetWorkerStateQuery(workerStateId);
        ResponseEntity<WorkerStateResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar estado do trabalhador",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createWorkerState(
        @Valid @RequestBody WorkerStateRequestDTO createWorkerStateRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateWorkerStateCommand(createWorkerStateRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{workerStateId}")
    @Operation(
        summary = "Atualizar estado do trabalhador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WorkerStateResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WorkerStateResponseDTO> updateWorkerState(
        @Valid @RequestBody WorkerStateRequestDTO updateWorkerStateRequest,
        @PathVariable(value = "workerStateId") String workerStateId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateWorkerStateCommand(updateWorkerStateRequest, workerStateId);
        ResponseEntity<WorkerStateResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{workerStateId}")
    @Operation(
        summary = "Desativar estado do trabalhador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteWorkerState(
        @PathVariable(value = "workerStateId") String workerStateId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarWorkerStateCommand(workerStateId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{workerStateId}/activate")
    @Operation(
        summary = "Ativar estado do trabalhador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateWorkerState(
        @PathVariable(value = "workerStateId") String workerStateId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarWorkerStateCommand(workerStateId);
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
    public ResponseEntity<List<ComboboxItemDTO>> getCombobox() {
        LOGGER.debug("Operation started");
        final var query = new GetWorkerStatesComboboxQuery();
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}