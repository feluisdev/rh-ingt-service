/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoDisciplinarCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.AccaoDisciplinarRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.TramitacaoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetProcessosDisciplinaresEmCursoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetTramitacaoDisciplinarQuery;
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
@RestController("colabsProcessoDisciplinarTramitacaoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "ProcessoDisciplinarTramitacao", description = "Tramitação do processo disciplinar (Estatuto Disciplinar — Lei n.º 31/III/87 e DL n.º 8/97): participação, instauração, instrução, suspensão preventiva, acusação, defesa, relatório, decisão, notificação, recurso e execução da pena, com os prazos")
public class ProcessoDisciplinarTramitacaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessoDisciplinarTramitacaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ProcessoDisciplinarTramitacaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("processos-disciplinares")
    @Operation(summary = "Os processos disciplinares em curso, com os prazos e os alertas")
    @ApiResponse(responseCode = "200", description = "Processos",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<List<TramitacaoDisciplinarDTO>> getProcessosDisciplinaresEmCurso() {
        return perguntar(new GetProcessosDisciplinaresEmCursoQuery());
    }

    @PostMapping("funcionarios/{funcionarioId}/processos-disciplinares/participacoes")
    @Operation(summary = "Registar a participação, queixa ou auto (art. 47.º): abre o processo com tramitação")
    @ApiResponse(responseCode = "201", description = "Participado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> participarProcessoDisciplinar(@PathVariable String funcionarioId,
                                                                                 @RequestBody AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, null, "PARTICIPAR", request));
    }

    @GetMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/tramitacao")
    @Operation(summary = "O processo com a tramitação: fase, actos, prazos e alertas")
    @ApiResponse(responseCode = "200", description = "Processo",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> getTramitacaoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId) {
        return perguntar(new GetTramitacaoDisciplinarQuery(funcionarioId, processoId));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/instaurar")
    @Operation(summary = "Instaurar (despacho liminar, art. 50.º); pode já nomear o instrutor")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> instaurarProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "INSTAURAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/instrutor")
    @Operation(summary = "Nomear ou substituir o instrutor (arts. 51.º, 54.º, 55.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> nomearInstrutorProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "NOMEAR_INSTRUTOR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/iniciar-instrucao")
    @Operation(summary = "Iniciar a instrução (em 3 dias úteis da nomeação; dura 30 dias — art. 48.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> iniciarInstrucaoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "INICIAR_INSTRUCAO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/prorrogar-instrucao")
    @Operation(summary = "Prorrogar a instrução, uma vez, até 30 dias (15 por omissão)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> prorrogarInstrucaoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "PRORROGAR_INSTRUCAO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/suspensao-preventiva")
    @Operation(summary = "Suspensão preventiva até 90 dias, com ou sem perda do vencimento de exercício (art. 56.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> suspenderPreventivamenteProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "SUSPENDER", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/levantar-suspensao")
    @Operation(summary = "Levantar a suspensão preventiva antes do fim")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> levantarSuspensaoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "LEVANTAR_SUSPENSAO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/acusar")
    @Operation(summary = "Deduzir a acusação, com a pena aplicável (arts. 60.º, 61.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> acusarProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "ACUSAR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/notificar-acusacao")
    @Operation(summary = "Notificar a acusação e marcar o prazo de defesa (10 a 20 dias, até 45 se complexo — art. 62.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> notificarAcusacaoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "NOTIFICAR_ACUSACAO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/defesa")
    @Operation(summary = "Registar a defesa do arguido (dentro do prazo)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> registarDefesaProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "DEFESA", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/relatorio")
    @Operation(summary = "Relatório final do instrutor: a pena proposta ou o arquivamento (arts. 60.º, 71.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> relatorioProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "RELATORIO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/decidir")
    @Operation(summary = "Decidir: aplicar a pena ou arquivar (art. 72.º); fundamentada se não concorda com o relatório")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> decidirProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "DECIDIR", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/notificar-decisao")
    @Operation(summary = "Notificar a decisão (a pena produz efeitos no dia seguinte — art. 77.º)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> notificarDecisaoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "NOTIFICAR_DECISAO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/recurso")
    @Operation(summary = "Interpor recurso hierárquico em 15 dias (art. 84.º; suspende a execução)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> recursoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "RECURSO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/decidir-recurso")
    @Operation(summary = "Decidir o recurso: manter, diminuir ou anular a pena")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> decidirRecursoProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "DECIDIR_RECURSO", request));
    }

    @PatchMapping("funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/arquivar")
    @Operation(summary = "Arquivar antes da decisão, com motivo")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = TramitacaoDisciplinarDTO.class)))
    public ResponseEntity<TramitacaoDisciplinarDTO> arquivarProcessoDisciplinar(@PathVariable String funcionarioId, @PathVariable String processoId,
            @RequestBody(required = false) AccaoDisciplinarRequestDTO request) {
        return enviar(new AccaoDisciplinarCommand(funcionarioId, processoId, "ARQUIVAR", request));
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
