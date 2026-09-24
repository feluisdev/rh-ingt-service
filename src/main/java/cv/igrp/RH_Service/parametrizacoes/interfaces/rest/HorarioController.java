/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioRequestDTO;
import java.util.List;

import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/horarios")
@Tag(name = "Horario", description = "Catálogo de horários da instituição (Lei n.º 20/X/2023, arts. 164.º a 166.º)")
public class HorarioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HorarioController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public HorarioController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar horários",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HorarioResponseDTO.class)))
            )
        }
    )
    public ResponseEntity<List<HorarioResponseDTO>> listHorarios(
        @RequestParam(value = "isActive", required = false) Boolean isActive) {

        LOGGER.debug("Operation started");

        final var query = new ListHorariosQuery(isActive);
        ResponseEntity<List<HorarioResponseDTO>> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{horarioId}")
    @Operation(
        summary = "Obter horário por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HorarioResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<HorarioResponseDTO> getHorarioById(
        @PathVariable(value = "horarioId") String horarioId) {

        LOGGER.debug("Operation started");

        final var query = new GetHorarioQuery(horarioId);
        ResponseEntity<HorarioResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar horário",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDTO.class))
            )
        }
    )
    public ResponseEntity<SuccessResponseDTO> createHorario(
        @Valid @RequestBody HorarioRequestDTO createHorarioRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateHorarioCommand(createHorarioRequest);
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{horarioId}")
    @Operation(
        summary = "Actualizar horário",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HorarioResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<HorarioResponseDTO> updateHorario(
        @Valid @RequestBody HorarioRequestDTO updateHorarioRequest,
        @PathVariable(value = "horarioId") String horarioId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateHorarioCommand(updateHorarioRequest, horarioId);
        ResponseEntity<HorarioResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{horarioId}")
    @Operation(
        summary = "Desativar horário",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDTO.class))
            )
        }
    )
    public ResponseEntity<SuccessResponseDTO> desativarHorario(
        @PathVariable(value = "horarioId") String horarioId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarHorarioCommand(horarioId);
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{horarioId}/activate")
    @Operation(
        summary = "Ativar horário",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDTO.class))
            )
        }
    )
    public ResponseEntity<SuccessResponseDTO> activateHorario(
        @PathVariable(value = "horarioId") String horarioId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarHorarioCommand(horarioId);
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{horarioId}/base")
    @Operation(
        summary = "Marcar horário base da instituição",
        description = "Vale para quem não tem horário na pessoa nem em nenhuma unidade da cadeia; a partir de hoje, ou de 'desde' (futura); os dias passados continuam com o anterior",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HorarioResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<HorarioResponseDTO> marcarHorarioBase(
        @PathVariable(value = "horarioId") String horarioId,
        @RequestParam(value = "desde", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate desde) {

        LOGGER.debug("Operation started");

        final var command = new MarcarHorarioBaseCommand(horarioId, desde);
        ResponseEntity<HorarioResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping("{horarioId}/duplicar")
    @Operation(
        summary = "Duplicar um horário",
        description = "Uma cópia editável: um horário que já vigorou não se edita, duplica-se e atribui-se o novo a partir de uma data",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HorarioResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<HorarioResponseDTO> duplicarHorario(
        @PathVariable(value = "horarioId") String horarioId,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");

        final var command = new DuplicarHorarioCommand(horarioId, nome);
        ResponseEntity<HorarioResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
