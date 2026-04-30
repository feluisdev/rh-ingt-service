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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaPublicHolidayDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/public-holidays")
@Tag(name = "PublicHoliday", description = "Gestão de feriados nacionais e municipais")
public class PublicHolidayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicHolidayController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public PublicHolidayController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar feriados",
        description = "Retorna a lista paginada de feriados com filtros por ano, tipo e intervalo de datas",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaPublicHolidayDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaPublicHolidayDTO> listPublicHolidays(
        @RequestParam(value = "year", required = false) Integer year,
        @RequestParam(value = "isNational", required = false) Boolean isNational,
        @RequestParam(value = "dateFrom", required = false) String dateFrom,
        @RequestParam(value = "dateTo", required = false) String dateTo,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new ListPublicHolidaysQuery(year, isNational, dateFrom, dateTo, isActive, pagina, tamanho);
        ResponseEntity<WrapperListaPublicHolidayDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{publicHolidayId}")
    @Operation(
        summary = "Obter feriado por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PublicHolidayResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<PublicHolidayResponseDTO> getPublicHolidayById(
        @PathVariable(value = "publicHolidayId") String publicHolidayId) {

        LOGGER.debug("Operation started");

        final var query = new GetPublicHolidayQuery(publicHolidayId);
        ResponseEntity<PublicHolidayResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar feriado",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createPublicHoliday(
        @Valid @RequestBody PublicHolidayRequestDTO createPublicHolidayRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreatePublicHolidayCommand(createPublicHolidayRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{publicHolidayId}")
    @Operation(
        summary = "Actualizar feriado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PublicHolidayResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<PublicHolidayResponseDTO> updatePublicHoliday(
        @Valid @RequestBody PublicHolidayRequestDTO updatePublicHolidayRequest,
        @PathVariable(value = "publicHolidayId") String publicHolidayId) {

        LOGGER.debug("Operation started");

        final var command = new UpdatePublicHolidayCommand(updatePublicHolidayRequest, publicHolidayId);
        ResponseEntity<PublicHolidayResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{publicHolidayId}")
    @Operation(
        summary = "Desativar feriado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deletePublicHoliday(
        @PathVariable(value = "publicHolidayId") String publicHolidayId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarPublicHolidayCommand(publicHolidayId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{publicHolidayId}/activate")
    @Operation(
        summary = "Ativar feriado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activatePublicHoliday(
        @PathVariable(value = "publicHolidayId") String publicHolidayId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarPublicHolidayCommand(publicHolidayId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
