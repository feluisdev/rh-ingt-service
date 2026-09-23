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
import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasRequestDTO;
import java.util.List;

import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/parametros-ferias")
@Tag(name = "ParametroFerias", description = "Parâmetros do mapa de férias por vigência (DL n.º 3/2010, arts. 5.º e 6.º)")
public class ParametroFeriasController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParametroFeriasController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ParametroFeriasController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar vigências dos parâmetros de férias",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ParametroFeriasResponseDTO.class)))
            )
        }
    )
    public ResponseEntity<List<ParametroFeriasResponseDTO>> listParametrosFerias() {

        LOGGER.debug("Operation started");

        final var query = new ListParametrosFeriasQuery();
        ResponseEntity<List<ParametroFeriasResponseDTO>> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("vigente")
    @Operation(
        summary = "Parâmetros de férias em vigor num ano",
        description = "Da tabela, ou os valores da lei (origem LEI) se nenhuma linha vigorar no ano",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ParametroFeriasResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ParametroFeriasResponseDTO> getParametroFeriasVigente(
        @RequestParam(value = "ano", required = false) Integer ano) {

        LOGGER.debug("Operation started");

        final var query = new GetParametroFeriasVigenteQuery(ano);
        ResponseEntity<ParametroFeriasResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar vigência dos parâmetros de férias",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponseDTO.class))
            )
        }
    )
    public ResponseEntity<SuccessResponseDTO> createParametroFerias(
        @Valid @RequestBody ParametroFeriasRequestDTO createParametroFeriasRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateParametroFeriasCommand(createParametroFeriasRequest);
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{parametroFeriasId}")
    @Operation(
        summary = "Actualizar vigência dos parâmetros de férias",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ParametroFeriasResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ParametroFeriasResponseDTO> updateParametroFerias(
        @Valid @RequestBody ParametroFeriasRequestDTO updateParametroFeriasRequest,
        @PathVariable(value = "parametroFeriasId") String parametroFeriasId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateParametroFeriasCommand(updateParametroFeriasRequest, parametroFeriasId);
        ResponseEntity<ParametroFeriasResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
