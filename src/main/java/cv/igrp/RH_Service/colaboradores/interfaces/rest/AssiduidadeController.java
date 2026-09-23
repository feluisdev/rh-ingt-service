/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AnularMarcacaoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.ImportarMarcacoesCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.LancarMarcacaoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.AnularMarcacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FaltasApuradasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ImportacaoMarcacoesRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ImportacaoMarcacoesResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetAssiduidadeQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetFaltasApuradasQuery;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@IgrpController
@RestController("colabsAssiduidadeController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Assiduidade", description = "Registo diário de assiduidade: marcações, importação de relógio e consulta (Lei n.º 20/X/2023, art. 164.º n.º 3)")
public class AssiduidadeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssiduidadeController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public AssiduidadeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("funcionarios/{funcionarioId}/marcacoes")
    @Operation(summary = "Lançar uma marcação (RH); num dia que já tem marcações é uma correcção e exige motivo")
    @ApiResponse(responseCode = "201", description = "Marcação registada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> lancarMarcacao(
            @PathVariable String funcionarioId, @RequestBody MarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new LancarMarcacaoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/marcacoes/{marcacaoId}/anular")
    @Operation(summary = "Anular uma marcação, com motivo; fica visível, anulada")
    @ApiResponse(responseCode = "200", description = "Marcação anulada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> anularMarcacao(
            @PathVariable String funcionarioId, @PathVariable String marcacaoId,
            @RequestBody AnularMarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new AnularMarcacaoCommand(funcionarioId, marcacaoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("assiduidade/importacao")
    @Operation(summary = "Importar picagens de um relógio; repetível pela referência externa")
    @ApiResponse(responseCode = "200", description = "Relatório da importação",
            content = @Content(schema = @Schema(implementation = ImportacaoMarcacoesResponseDTO.class)))
    public ResponseEntity<ImportacaoMarcacoesResponseDTO> importarMarcacoes(@RequestBody ImportacaoMarcacoesRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<ImportacaoMarcacoesResponseDTO> response = commandBus.send(new ImportarMarcacoesCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionarios/{funcionarioId}/assiduidade")
    @Operation(summary = "Assiduidade de um período: por dia (períodos, intervalos, horas, anomalias) e por semana")
    @ApiResponse(responseCode = "200", description = "Assiduidade do período",
            content = @Content(schema = @Schema(implementation = AssiduidadeResponseDTO.class)))
    public ResponseEntity<AssiduidadeResponseDTO> getAssiduidade(
            @PathVariable String funcionarioId,
            @RequestParam(value = "de") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(value = "ate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LOGGER.debug("Operation started");
        ResponseEntity<AssiduidadeResponseDTO> response = queryBus.handle(new GetAssiduidadeQuery(funcionarioId, de, ate));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionarios/{funcionarioId}/faltas-apuradas")
    @Operation(summary = "Faltas por débito do mês (DL n.º 3/2010, art. 13.º): por dia, débitos da aferição e conversão em dias")
    @ApiResponse(responseCode = "200", description = "Apuramento do mês",
            content = @Content(schema = @Schema(implementation = FaltasApuradasResponseDTO.class)))
    public ResponseEntity<FaltasApuradasResponseDTO> getFaltasApuradas(
            @PathVariable String funcionarioId, @RequestParam(value = "mes") String mes) {
        LOGGER.debug("Operation started");
        ResponseEntity<FaltasApuradasResponseDTO> response = queryBus.handle(new GetFaltasApuradasQuery(funcionarioId, mes));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/marcacoes/{marcacaoId}/validar")
    @Operation(summary = "Validar um pedido de correcao (RH)")
    @ApiResponse(responseCode = "200", description = "Validado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> validarMarcacao(@PathVariable String funcionarioId, @PathVariable String marcacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new cv.igrp.RH_Service.colaboradores.application.commands.DecidirMarcacaoCommand(false, funcionarioId, marcacaoId, true, null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("funcionarios/{funcionarioId}/marcacoes/{marcacaoId}/rejeitar")
    @Operation(summary = "Rejeitar um pedido de correcao (RH), com motivo")
    @ApiResponse(responseCode = "200", description = "Rejeitado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> rejeitarMarcacao(@PathVariable String funcionarioId, @PathVariable String marcacaoId,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.DecisaoMarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new cv.igrp.RH_Service.colaboradores.application.commands.DecidirMarcacaoCommand(false, funcionarioId, marcacaoId, false, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
