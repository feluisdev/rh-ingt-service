/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoPeriodoProvaCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.RegistarProvimentoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.EntradaServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoProvaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProvimentoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProvimentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetEntradaServicoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetTutoriasQuery;
import cv.igrp.framework.core.domain.Command;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.Query;
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

import java.util.List;

@IgrpController
@RestController("colabsEntradaServicoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "EntradaServico", description = "Entrada ao serviço (Lei n.º 20/X/2023, arts. 52.º–81.º): provimento e posse, estágio probatório (1 ano, tutor, relatório) e período experimental (60/30 dias)")
public class EntradaServicoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntradaServicoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public EntradaServicoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("funcionarios/{funcionarioId}/provimentos")
    @Operation(summary = "Registar o provimento (forma de vínculo, despacho e posse); abre o estágio probatório ou o período experimental")
    @ApiResponse(responseCode = "201", description = "Provimento registado",
            content = @Content(schema = @Schema(implementation = ProvimentoDTO.class)))
    public ResponseEntity<ProvimentoDTO> registarProvimento(@PathVariable String funcionarioId, @RequestBody ProvimentoRequestDTO request) {
        return enviar(new RegistarProvimentoCommand(funcionarioId, request));
    }

    @GetMapping("funcionarios/{funcionarioId}/provimentos")
    @Operation(summary = "Os provimentos e os períodos de prova do colaborador")
    @ApiResponse(responseCode = "200", description = "Entrada ao serviço",
            content = @Content(schema = @Schema(implementation = EntradaServicoDTO.class)))
    public ResponseEntity<EntradaServicoDTO> getEntradaServico(@PathVariable String funcionarioId) {
        return perguntar(new GetEntradaServicoQuery(funcionarioId));
    }

    @PatchMapping("funcionarios/{funcionarioId}/periodos-prova/{periodoId}/concluir")
    @Operation(summary = "Decidir no fim do período (por omissão, com a avaliação do relatório do tutor)")
    @ApiResponse(responseCode = "200", description = "Concluído",
            content = @Content(schema = @Schema(implementation = PeriodoProvaDTO.class)))
    public ResponseEntity<PeriodoProvaDTO> concluirPeriodoProva(@PathVariable String funcionarioId, @PathVariable String periodoId,
                                                                @RequestBody PeriodoProvaRequestDTO request) {
        return enviar(new AccaoPeriodoProvaCommand(funcionarioId, periodoId, "CONCLUIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/periodos-prova/{periodoId}/cessar")
    @Operation(summary = "Cessação antecipada por relatório (acto) fundamentado")
    @ApiResponse(responseCode = "200", description = "Cessado",
            content = @Content(schema = @Schema(implementation = PeriodoProvaDTO.class)))
    public ResponseEntity<PeriodoProvaDTO> cessarPeriodoProva(@PathVariable String funcionarioId, @PathVariable String periodoId,
                                                              @RequestBody PeriodoProvaRequestDTO request) {
        return enviar(new AccaoPeriodoProvaCommand(funcionarioId, periodoId, "CESSAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/periodos-prova/{periodoId}/denunciar")
    @Operation(summary = "Registar a denúncia do contrato pelo agente no período experimental (sem aviso prévio)")
    @ApiResponse(responseCode = "200", description = "Denunciado",
            content = @Content(schema = @Schema(implementation = PeriodoProvaDTO.class)))
    public ResponseEntity<PeriodoProvaDTO> denunciarPeriodoProva(@PathVariable String funcionarioId, @PathVariable String periodoId,
                                                                 @RequestBody PeriodoProvaRequestDTO request) {
        return enviar(new AccaoPeriodoProvaCommand(funcionarioId, periodoId, "DENUNCIAR", request));
    }

    @GetMapping("me/tutorias")
    @Operation(summary = "Os estágios probatórios em curso de que sou tutor")
    @ApiResponse(responseCode = "200", description = "Estágios",
            content = @Content(schema = @Schema(implementation = PeriodoProvaDTO.class)))
    public ResponseEntity<List<PeriodoProvaDTO>> getMinhasTutorias() {
        return perguntar(new GetTutoriasQuery());
    }

    @PatchMapping("me/tutorias/{periodoId}/relatorio")
    @Operation(summary = "Remeter o relatório final do estágio (o tutor): avaliação e fundamentação")
    @ApiResponse(responseCode = "200", description = "Relatório registado",
            content = @Content(schema = @Schema(implementation = PeriodoProvaDTO.class)))
    public ResponseEntity<PeriodoProvaDTO> remeterRelatorioEstagio(@PathVariable String periodoId, @RequestBody PeriodoProvaRequestDTO request) {
        return enviar(new AccaoPeriodoProvaCommand(null, periodoId, "RELATORIO", request));
    }

    private <T> ResponseEntity<T> enviar(Command command) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    private <T> ResponseEntity<T> perguntar(Query query) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
