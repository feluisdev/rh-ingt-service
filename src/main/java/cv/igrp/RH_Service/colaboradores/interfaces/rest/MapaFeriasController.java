/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.IndicarPreferenciaFeriasCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.MarcarFeriasCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.PublicarMapaFeriasCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriasMarcacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriasPreferenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MapaFeriasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetFeriasDoAnoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetMapaFeriasQuery;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@IgrpController
@RestController("colabsMapaFeriasController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "MapaFerias", description = "Mapa de férias: preferência, marcação e publicação (DL n.º 3/2010, arts. 5.º e 6.º)")
public class MapaFeriasController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapaFeriasController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public MapaFeriasController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("funcionarios/{funcionarioId}/ferias/{ano}")
    @Operation(summary = "Férias do colaborador no ano: preferência, marcação e alterações")
    @ApiResponse(responseCode = "200", description = "Férias do ano",
            content = @Content(schema = @Schema(implementation = FeriasAnoResponseDTO.class)))
    public ResponseEntity<FeriasAnoResponseDTO> getFeriasDoAno(
            @PathVariable String funcionarioId, @PathVariable Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<FeriasAnoResponseDTO> response = queryBus.handle(new GetFeriasDoAnoQuery(funcionarioId, ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("funcionarios/{funcionarioId}/ferias/{ano}/preferencia")
    @Operation(summary = "Indicar a preferência de férias (art. 5.º n.º 4); fora do prazo é aceite com alerta")
    @ApiResponse(responseCode = "200", description = "Preferência registada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> indicarPreferencia(
            @PathVariable String funcionarioId, @PathVariable Integer ano,
            @RequestBody FeriasPreferenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new IndicarPreferenciaFeriasCommand(funcionarioId, ano, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("funcionarios/{funcionarioId}/ferias/{ano}/marcacao")
    @Operation(summary = "Marcar as férias do ano (art. 5.º); depois de publicado o mapa exige o motivo do art. 6.º n.º 2")
    @ApiResponse(responseCode = "200", description = "Marcação registada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> marcar(
            @PathVariable String funcionarioId, @PathVariable Integer ano,
            @RequestBody FeriasMarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new MarcarFeriasCommand(funcionarioId, ano, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("ferias/mapa/{ano}")
    @Operation(summary = "Mapa de férias do ano: marcações e quem ainda não tem nenhuma")
    @ApiResponse(responseCode = "200", description = "Mapa de férias",
            content = @Content(schema = @Schema(implementation = MapaFeriasResponseDTO.class)))
    public ResponseEntity<MapaFeriasResponseDTO> getMapa(@PathVariable Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<MapaFeriasResponseDTO> response = queryBus.handle(new GetMapaFeriasQuery(ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("ferias/mapa/{ano}/publicar")
    @Operation(summary = "Dar conhecimento do mapa de férias (art. 6.º n.º 1); não há aprovação")
    @ApiResponse(responseCode = "201", description = "Mapa dado a conhecer",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> publicar(@PathVariable Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new PublicarMapaFeriasCommand(ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
