/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.CancelarTrabalhoSuplementarCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.DecidirTrabalhoSuplementarCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.LancarTrabalhoSuplementarCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoTrabalhoSuplementarRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarMesDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetTrabalhoSuplementarMesQuery;
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
@RestController("colabsTrabalhoSuplementarController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "TrabalhoSuplementar", description = "Trabalho suplementar (horas extras): autorização, horas realizadas pelas marcações e totais do mês, sem valores (Lei n.º 20/X/2023, art. 155.º n.º 2 a))")
public class TrabalhoSuplementarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrabalhoSuplementarController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public TrabalhoSuplementarController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("funcionarios/{funcionarioId}/trabalho-suplementar")
    @Operation(summary = "Lançar trabalho suplementar (RH); nasce autorizado, também para um dia passado")
    @ApiResponse(responseCode = "201", description = "Trabalho suplementar autorizado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> lancarTrabalhoSuplementar(
            @PathVariable String funcionarioId, @RequestBody TrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new LancarTrabalhoSuplementarCommand(false, funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionarios/{funcionarioId}/trabalho-suplementar")
    @Operation(summary = "Trabalho suplementar do mês: cada um com o tipo de dia e as horas realizadas; totais dos autorizados")
    @ApiResponse(responseCode = "200", description = "Trabalho suplementar do mês",
            content = @Content(schema = @Schema(implementation = TrabalhoSuplementarMesDTO.class)))
    public ResponseEntity<TrabalhoSuplementarMesDTO> getTrabalhoSuplementar(
            @PathVariable String funcionarioId, @RequestParam(value = "mes") String mes) {
        LOGGER.debug("Operation started");
        ResponseEntity<TrabalhoSuplementarMesDTO> response = queryBus.handle(new GetTrabalhoSuplementarMesQuery(funcionarioId, mes));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/trabalho-suplementar/{trabalhoId}/autorizar")
    @Operation(summary = "Autorizar um pedido de trabalho suplementar (RH)")
    @ApiResponse(responseCode = "200", description = "Autorizado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> autorizarTrabalhoSuplementar(
            @PathVariable String funcionarioId, @PathVariable String trabalhoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new DecidirTrabalhoSuplementarCommand(false, funcionarioId, trabalhoId, true, null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/trabalho-suplementar/{trabalhoId}/recusar")
    @Operation(summary = "Recusar um pedido de trabalho suplementar (RH), com motivo")
    @ApiResponse(responseCode = "200", description = "Recusado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> recusarTrabalhoSuplementar(
            @PathVariable String funcionarioId, @PathVariable String trabalhoId,
            @RequestBody DecisaoTrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new DecidirTrabalhoSuplementarCommand(false, funcionarioId, trabalhoId, false, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/trabalho-suplementar/{trabalhoId}/cancelar")
    @Operation(summary = "Cancelar trabalho suplementar pedido ou autorizado (RH), com motivo")
    @ApiResponse(responseCode = "200", description = "Cancelado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> cancelarTrabalhoSuplementar(
            @PathVariable String funcionarioId, @PathVariable String trabalhoId,
            @RequestBody DecisaoTrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new CancelarTrabalhoSuplementarCommand(funcionarioId, trabalhoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
