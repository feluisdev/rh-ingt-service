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
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO;
import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.PositionRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/estrutura/positions")
@Tag(name = "Position", description = "Mapa de Pessoal — gestão de Lugares")
public class PositionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public PositionController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar Lugares de uma unidade",
        description = "Retorna os Lugares (Mapa de Pessoal) de uma unidade orgânica",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaPositionDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaPositionDTO> getPositions(
        @RequestParam(value = "unidadeId") String unidadeId) {

        LOGGER.debug("Operation started");

        final var query = new GetPositionsByUnidadeQuery(unidadeId);
        ResponseEntity<WrapperListaPositionDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{positionId}")
    @Operation(
        summary = "Obter Lugar por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PositionResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<PositionResponseDTO> getPositionById(
        @PathVariable(value = "positionId") String positionId) {

        LOGGER.debug("Operation started");

        final var query = new GetPositionByIdQuery(positionId);
        ResponseEntity<PositionResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar Lugar",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createPosition(
        @Valid @RequestBody PositionRequestDTO createPositionRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreatePositionCommand(createPositionRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{positionId}")
    @Operation(
        summary = "Atualizar Lugar",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PositionResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<PositionResponseDTO> updatePosition(
        @Valid @RequestBody PositionRequestDTO updatePositionRequest,
        @PathVariable(value = "positionId") String positionId) {

        LOGGER.debug("Operation started");

        final var command = new UpdatePositionCommand(updatePositionRequest, positionId);
        ResponseEntity<PositionResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{positionId}/freeze")
    @Operation(
        summary = "Congelar Lugar",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> freezePosition(
        @PathVariable(value = "positionId") String positionId) {

        LOGGER.debug("Operation started");

        final var command = new CongelarPositionCommand(positionId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{positionId}")
    @Operation(
        summary = "Extinguir Lugar",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> extinguishPosition(
        @PathVariable(value = "positionId") String positionId) {

        LOGGER.debug("Operation started");

        final var command = new ExtinguirPositionCommand(positionId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
