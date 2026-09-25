/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AbrirProcessoAposentacaoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.DecidirProcessoAposentacaoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.DecidirProrrogacaoPermanenciaCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.PedirProrrogacaoPermanenciaCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoAposentacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.RelatorioAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.SituacaoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetRelatorioAposentacaoCsvQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetRelatorioAposentacaoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetSituacaoAposentacaoQuery;
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
@RestController("colabsAposentacaoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Aposentacao", description = "Aposentação e limite de idade (Lei n.º 20/X/2023, arts. 48.º e 173.º–179.º): situação de cada colaborador, relatório por serviço, processo de aposentação e prorrogação para além dos 65 anos")
public class AposentacaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AposentacaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public AposentacaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("funcionarios/{funcionarioId}/aposentacao")
    @Operation(summary = "A situação do colaborador perante a aposentação: limite de idade, tempo de serviço, condições, processos e prorrogações")
    @ApiResponse(responseCode = "200", description = "Situação",
            content = @Content(schema = @Schema(implementation = SituacaoAposentacaoDTO.class)))
    public ResponseEntity<SituacaoAposentacaoDTO> getSituacaoAposentacao(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SituacaoAposentacaoDTO> response = queryBus.handle(new GetSituacaoAposentacaoQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("funcionarios/{funcionarioId}/aposentacao/processos")
    @Operation(summary = "Abrir um processo de aposentação (RH)")
    @ApiResponse(responseCode = "201", description = "Processo aberto",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> abrirProcessoAposentacao(
            @PathVariable String funcionarioId, @RequestBody ProcessoAposentacaoRequestDTO request) {
        return enviar(new AbrirProcessoAposentacaoCommand(false, funcionarioId, request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/deferir")
    @Operation(summary = "Deferir o processo: o despacho que autoriza")
    @ApiResponse(responseCode = "200", description = "Deferido",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> deferirProcessoAposentacao(
            @PathVariable String funcionarioId, @PathVariable String processoId, @RequestBody DecisaoAposentacaoRequestDTO request) {
        return enviar(new DecidirProcessoAposentacaoCommand(funcionarioId, processoId, "DEFERIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/indeferir")
    @Operation(summary = "Indeferir o processo, com motivo")
    @ApiResponse(responseCode = "200", description = "Indeferido",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> indeferirProcessoAposentacao(
            @PathVariable String funcionarioId, @PathVariable String processoId, @RequestBody DecisaoAposentacaoRequestDTO request) {
        return enviar(new DecidirProcessoAposentacaoCommand(funcionarioId, processoId, "INDEFERIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/desligar")
    @Operation(summary = "Desligar do serviço aguardando a aposentação, ou iniciar a pré-aposentação (com a percentagem da prestação)")
    @ApiResponse(responseCode = "200", description = "Desligado",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> desligarProcessoAposentacao(
            @PathVariable String funcionarioId, @PathVariable String processoId, @RequestBody DecisaoAposentacaoRequestDTO request) {
        return enviar(new DecidirProcessoAposentacaoCommand(funcionarioId, processoId, "DESLIGAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/concluir")
    @Operation(summary = "Concluir: a aposentação cessa o vínculo na data indicada")
    @ApiResponse(responseCode = "200", description = "Aposentado",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> concluirProcessoAposentacao(
            @PathVariable String funcionarioId, @PathVariable String processoId, @RequestBody DecisaoAposentacaoRequestDTO request) {
        return enviar(new DecidirProcessoAposentacaoCommand(funcionarioId, processoId, "CONCLUIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/cancelar")
    @Operation(summary = "Cancelar o processo antes da desligação, com motivo")
    @ApiResponse(responseCode = "200", description = "Cancelado",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> cancelarProcessoAposentacao(
            @PathVariable String funcionarioId, @PathVariable String processoId, @RequestBody DecisaoAposentacaoRequestDTO request) {
        return enviar(new DecidirProcessoAposentacaoCommand(funcionarioId, processoId, "CANCELAR", request));
    }

    @PostMapping("funcionarios/{funcionarioId}/aposentacao/prorrogacoes")
    @Operation(summary = "Pedir a permanência ao serviço para além dos 65 anos (até aos 70)")
    @ApiResponse(responseCode = "201", description = "Pedido registado",
            content = @Content(schema = @Schema(implementation = ProrrogacaoPermanenciaDTO.class)))
    public ResponseEntity<ProrrogacaoPermanenciaDTO> pedirProrrogacaoPermanencia(
            @PathVariable String funcionarioId, @RequestBody ProrrogacaoPermanenciaRequestDTO request) {
        return enviar(new PedirProrrogacaoPermanenciaCommand(funcionarioId, request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/prorrogacoes/{prorrogacaoId}/autorizar")
    @Operation(summary = "Autorizar a permanência (despacho)")
    @ApiResponse(responseCode = "200", description = "Autorizada",
            content = @Content(schema = @Schema(implementation = ProrrogacaoPermanenciaDTO.class)))
    public ResponseEntity<ProrrogacaoPermanenciaDTO> autorizarProrrogacaoPermanencia(
            @PathVariable String funcionarioId, @PathVariable String prorrogacaoId, @RequestBody ProrrogacaoPermanenciaRequestDTO request) {
        return enviar(new DecidirProrrogacaoPermanenciaCommand(funcionarioId, prorrogacaoId, true, request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/aposentacao/prorrogacoes/{prorrogacaoId}/indeferir")
    @Operation(summary = "Indeferir a permanência, com motivo")
    @ApiResponse(responseCode = "200", description = "Indeferida",
            content = @Content(schema = @Schema(implementation = ProrrogacaoPermanenciaDTO.class)))
    public ResponseEntity<ProrrogacaoPermanenciaDTO> indeferirProrrogacaoPermanencia(
            @PathVariable String funcionarioId, @PathVariable String prorrogacaoId, @RequestBody ProrrogacaoPermanenciaRequestDTO request) {
        return enviar(new DecidirProrrogacaoPermanenciaCommand(funcionarioId, prorrogacaoId, false, request));
    }

    @GetMapping("relatorios/aposentacao")
    @Operation(summary = "Quem, no serviço, atinge o limite de idade ou reúne as condições da antecipada ou da pré-aposentação até uma data (por omissão, 12 meses)")
    @ApiResponse(responseCode = "200", description = "Relatório",
            content = @Content(schema = @Schema(implementation = RelatorioAposentacaoDTO.class)))
    public ResponseEntity<RelatorioAposentacaoDTO> getRelatorioAposentacao(
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades,
            @RequestParam(value = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LOGGER.debug("Operation started");
        ResponseEntity<RelatorioAposentacaoDTO> response = queryBus.handle(new GetRelatorioAposentacaoQuery(unidadeId, incluirSubunidades, ate));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping(value = "relatorios/aposentacao.csv", produces = "text/csv")
    @Operation(summary = "O mesmo relatório em CSV (separador ;, UTF-8 com BOM)")
    @ApiResponse(responseCode = "200", description = "Ficheiro CSV")
    public ResponseEntity<byte[]> getRelatorioAposentacaoCsv(
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades,
            @RequestParam(value = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LOGGER.debug("Operation started");
        ResponseEntity<byte[]> response = queryBus.handle(new GetRelatorioAposentacaoCsvQuery(unidadeId, incluirSubunidades, ate));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("me/aposentacao")
    @Operation(summary = "A minha situação perante a aposentação")
    @ApiResponse(responseCode = "200", description = "Situação",
            content = @Content(schema = @Schema(implementation = SituacaoAposentacaoDTO.class)))
    public ResponseEntity<SituacaoAposentacaoDTO> getMinhaSituacaoAposentacao() {
        LOGGER.debug("Operation started");
        ResponseEntity<SituacaoAposentacaoDTO> response = queryBus.handle(new GetSituacaoAposentacaoQuery(null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("me/aposentacao/processos")
    @Operation(summary = "Pedir a aposentação antecipada ou a pré-aposentação (o próprio)")
    @ApiResponse(responseCode = "201", description = "Pedido registado",
            content = @Content(schema = @Schema(implementation = ProcessoAposentacaoDTO.class)))
    public ResponseEntity<ProcessoAposentacaoDTO> pedirMinhaAposentacao(@RequestBody ProcessoAposentacaoRequestDTO request) {
        return enviar(new AbrirProcessoAposentacaoCommand(true, null, request));
    }

    private <T> ResponseEntity<T> enviar(cv.igrp.framework.core.domain.Command command) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
