/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.formacao.interfaces.rest;

import cv.igrp.RH_Service.formacao.application.commands.AccaoFormacaoCommand;
import cv.igrp.RH_Service.formacao.application.commands.AccaoPlanoFormacaoCommand;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoRequestDTO;
import cv.igrp.RH_Service.formacao.application.dto.HorasFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoRequestDTO;
import cv.igrp.RH_Service.formacao.application.queries.GetAccaoFormacaoQuery;
import cv.igrp.RH_Service.formacao.application.queries.GetAccoesFormacaoQuery;
import cv.igrp.RH_Service.formacao.application.queries.GetHorasFormacaoQuery;
import cv.igrp.RH_Service.formacao.application.queries.GetPlanoFormacaoQuery;
import cv.igrp.RH_Service.formacao.application.queries.GetPlanosFormacaoQuery;
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
@RestController("formFormacaoProcessoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "FormacaoProcesso", description = "Formação como processo (Lei n.º 20/X/2023, art. 141.º): plano anual e necessidades, acções, inscrições, avaliação, prazo de garantia (art. 95.º b)) e horas de formação")
public class FormacaoProcessoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormacaoProcessoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FormacaoProcessoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("formacao/accoes")
    @Operation(summary = "As acções de formação (filtros: estado, ano)")
    @ApiResponse(responseCode = "200", description = "Acções",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<List<AccaoFormacaoDTO>> getAccoesFormacao(@RequestParam(value = "estado", required = false) String estado,
                                                                   @RequestParam(value = "ano", required = false) Integer ano) {
        return perguntar(new GetAccoesFormacaoQuery(estado, ano, false));
    }

    @GetMapping("formacao/accoes/{accaoId}")
    @Operation(summary = "Uma acção de formação com as inscrições")
    @ApiResponse(responseCode = "200", description = "Acção",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> getAccaoFormacao(@PathVariable String accaoId) {
        return perguntar(new GetAccaoFormacaoQuery(accaoId));
    }

    @GetMapping("me/formacao/accoes")
    @Operation(summary = "As acções com inscrições abertas e as minhas (só a minha inscrição)")
    @ApiResponse(responseCode = "200", description = "Acções",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<List<AccaoFormacaoDTO>> getMinhasAccoesFormacao() {
        return perguntar(new GetAccoesFormacaoQuery(null, null, true));
    }

    @GetMapping("formacao/planos")
    @Operation(summary = "Os planos de formação (filtro: ano)")
    @ApiResponse(responseCode = "200", description = "Planos",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<List<PlanoFormacaoDTO>> getPlanosFormacao(@RequestParam(value = "ano", required = false) Integer ano) {
        return perguntar(new GetPlanosFormacaoQuery(ano));
    }

    @GetMapping("formacao/planos/{planoId}")
    @Operation(summary = "Um plano de formação com as necessidades")
    @ApiResponse(responseCode = "200", description = "Plano",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<PlanoFormacaoDTO> getPlanoFormacao(@PathVariable String planoId) {
        return perguntar(new GetPlanoFormacaoQuery(planoId));
    }

    @GetMapping("formacao/horas")
    @Operation(summary = "As horas de formação por colaborador no ano (pelo histórico; indicador do balanço social)")
    @ApiResponse(responseCode = "200", description = "Horas",
            content = @Content(schema = @Schema(implementation = HorasFormacaoDTO.class)))
    public ResponseEntity<List<HorasFormacaoDTO>> getHorasFormacao(@RequestParam(value = "ano", required = false) Integer ano) {
        return perguntar(new GetHorasFormacaoQuery(ano));
    }

    @PostMapping("formacao/accoes")
    @Operation(summary = "Planear uma acção de formação (pode responder a necessidades de um plano aprovado)")
    @ApiResponse(responseCode = "201", description = "Planeada",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> planearAccaoFormacao(@RequestBody AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(null, null, "PLANEAR", request, false));
    }

    @PutMapping("formacao/accoes/{accaoId}")
    @Operation(summary = "Alterar os termos da acção (planeada ou com inscrições abertas; campos omitidos ficam)")
    @ApiResponse(responseCode = "200", description = "Alterada",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> actualizarAccaoFormacao(@PathVariable String accaoId, @RequestBody AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "ACTUALIZAR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/abrir-inscricoes")
    @Operation(summary = "Abrir as inscrições")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> abrirInscricoesAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "ABRIR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/iniciar")
    @Operation(summary = "Começar a acção (com formandos admitidos; os pedidos por decidir caem)")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> iniciarAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "INICIAR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/concluir")
    @Operation(summary = "Concluir: todos avaliados; os aproveitamentos entram no histórico do colaborador")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> concluirAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "CONCLUIR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/cancelar")
    @Operation(summary = "Cancelar a acção, com motivo")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> cancelarAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "CANCELAR", request, false));
    }

    @PostMapping("formacao/accoes/{accaoId}/inscricoes")
    @Operation(summary = "Inscrever um colaborador (pelo RH: fica admitido, dentro das vagas)")
    @ApiResponse(responseCode = "200", description = "Inscrito",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> inscreverAccaoFormacao(@PathVariable String accaoId, @RequestBody AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "INSCREVER", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/admitir")
    @Operation(summary = "Admitir o pedido de inscrição")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> admitirInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "ADMITIR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/recusar")
    @Operation(summary = "Recusar o pedido de inscrição, com motivo")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> recusarInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "RECUSAR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/desistir")
    @Operation(summary = "Registar a desistência")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> desistirInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "DESISTIR", request, false));
    }

    @PatchMapping("formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/avaliar")
    @Operation(summary = "Avaliar o formando: aproveitamento, sem aproveitamento ou faltou, e os dias de presença")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> avaliarInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "AVALIAR", request, false));
    }

    @PostMapping("me/formacao/accoes/{accaoId}/inscricao")
    @Operation(summary = "Pedir inscrição (o próprio) ou inscrever alguém da equipa directa (a chefia)")
    @ApiResponse(responseCode = "201", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> inscreverMeAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "INSCREVER", request, true));
    }

    @PatchMapping("me/formacao/accoes/{accaoId}/desistir")
    @Operation(summary = "Desistir da minha inscrição")
    @ApiResponse(responseCode = "200", description = "Registado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> desistirMeAccaoFormacao(@PathVariable String accaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, null, "DESISTIR", request, true));
    }

    @PatchMapping("me/formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/admitir")
    @Operation(summary = "A chefia directa admite o pedido de alguém da equipa")
    @ApiResponse(responseCode = "200", description = "Admitido",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> admitirMeInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "ADMITIR", request, true));
    }

    @PatchMapping("me/formacao/accoes/{accaoId}/inscricoes/{inscricaoId}/recusar")
    @Operation(summary = "A chefia directa recusa o pedido de alguém da equipa, com motivo")
    @ApiResponse(responseCode = "200", description = "Recusado",
            content = @Content(schema = @Schema(implementation = AccaoFormacaoDTO.class)))
    public ResponseEntity<AccaoFormacaoDTO> recusarMeInscricaoFormacao(@PathVariable String accaoId, @PathVariable String inscricaoId, @RequestBody(required = false) AccaoFormacaoRequestDTO request) {
        return enviar(new AccaoFormacaoCommand(accaoId, inscricaoId, "RECUSAR", request, true));
    }

    @PostMapping("formacao/planos")
    @Operation(summary = "Criar o plano anual de formação (em rascunho)")
    @ApiResponse(responseCode = "201", description = "Criado",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<PlanoFormacaoDTO> criarPlanoFormacao(@RequestBody PlanoFormacaoRequestDTO request) {
        return enviar(new AccaoPlanoFormacaoCommand(null, "CRIAR", request, false));
    }

    @PostMapping("formacao/planos/{planoId}/necessidades")
    @Operation(summary = "Identificar uma necessidade de formação (pelo RH)")
    @ApiResponse(responseCode = "200", description = "Identificada",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<PlanoFormacaoDTO> identificarNecessidadeFormacao(@PathVariable String planoId, @RequestBody PlanoFormacaoRequestDTO request) {
        return enviar(new AccaoPlanoFormacaoCommand(planoId, "NECESSIDADE", request, false));
    }

    @PatchMapping("formacao/planos/{planoId}/aprovar")
    @Operation(summary = "Aprovar o plano (despacho)")
    @ApiResponse(responseCode = "200", description = "Aprovado",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<PlanoFormacaoDTO> aprovarPlanoFormacao(@PathVariable String planoId, @RequestBody PlanoFormacaoRequestDTO request) {
        return enviar(new AccaoPlanoFormacaoCommand(planoId, "APROVAR", request, false));
    }

    @PostMapping("me/formacao/planos/{planoId}/necessidades")
    @Operation(summary = "Identificar uma necessidade minha (o próprio) ou da minha equipa directa (a chefia)")
    @ApiResponse(responseCode = "200", description = "Identificada",
            content = @Content(schema = @Schema(implementation = PlanoFormacaoDTO.class)))
    public ResponseEntity<PlanoFormacaoDTO> identificarMeNecessidadeFormacao(@PathVariable String planoId, @RequestBody PlanoFormacaoRequestDTO request) {
        return enviar(new AccaoPlanoFormacaoCommand(planoId, "NECESSIDADE", request, true));
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
