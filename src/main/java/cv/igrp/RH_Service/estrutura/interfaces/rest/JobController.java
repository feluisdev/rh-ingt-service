/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.estrutura.interfaces.rest;

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
import cv.igrp.RH_Service.estrutura.application.commands.*;
import cv.igrp.RH_Service.estrutura.application.queries.*;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaJobDTO;
import cv.igrp.RH_Service.estrutura.application.dto.JobResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.JobRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/estrutura/jobs")
@Tag(name = "Job", description = "Gestão de cargos funcionais")
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public JobController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar cargos",
        description = "Retorna a lista paginada de cargos",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaJobDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaJobDTO> getJobs(
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new GetJobsQuery(active, pagina, tamanho);
        ResponseEntity<WrapperListaJobDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{jobId}")
    @Operation(
        summary = "Obter cargo por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<JobResponseDTO> getJobById(
        @PathVariable(value = "jobId") String jobId) {

        LOGGER.debug("Operation started");

        final var query = new GetJobByIdQuery(jobId);
        ResponseEntity<JobResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar cargo",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createJob(
        @Valid @RequestBody JobRequestDTO createJobRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateJobCommand(createJobRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{jobId}")
    @Operation(
        summary = "Atualizar cargo",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<JobResponseDTO> updateJob(
        @Valid @RequestBody JobRequestDTO updateJobRequest,
        @PathVariable(value = "jobId") String jobId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateJobCommand(updateJobRequest, jobId);
        ResponseEntity<JobResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{jobId}/deactivate")
    @Operation(
        summary = "Desativar cargo",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deactivateJob(
        @PathVariable(value = "jobId") String jobId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarJobCommand(jobId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{jobId}/activate")
    @Operation(
        summary = "Ativar cargo",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateJob(
        @PathVariable(value = "jobId") String jobId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarJobCommand(jobId);
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
        final var query = new GetJobsComboboxQuery();
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}