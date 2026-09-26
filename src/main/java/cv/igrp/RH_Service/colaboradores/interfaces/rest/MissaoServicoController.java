/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoMissaoServicoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetMissaoServicoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetMissoesServicoQuery;
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
@RestController("colabsMissaoServicoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "MissaoServico", description = "Missões de serviço (Lei n.º 20/X/2023, art. 159.º): pedido, autorização, regresso com relatório e dias de ajudas de custo para o salarial")
public class MissaoServicoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissaoServicoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public MissaoServicoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("missoes-servico")
    @Operation(summary = "As missões de serviço (filtros: estado, participante)")
    @ApiResponse(responseCode = "200", description = "Missões",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<List<MissaoServicoDTO>> getMissoesServico(@RequestParam(value = "estado", required = false) String estado,
                                                                   @RequestParam(value = "funcionarioId", required = false) String funcionarioId) {
        return perguntar(new GetMissoesServicoQuery(estado, funcionarioId, false));
    }

    @GetMapping("missoes-servico/{missaoId}")
    @Operation(summary = "Uma missão de serviço, com os alertas (sobreposição com férias ou ausências)")
    @ApiResponse(responseCode = "200", description = "Missão",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> getMissaoServico(@PathVariable String missaoId) {
        return perguntar(new GetMissaoServicoQuery(missaoId));
    }

    @GetMapping("me/missoes-servico")
    @Operation(summary = "As minhas missões e as pedidas da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Missões",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<List<MissaoServicoDTO>> getMinhasMissoesServico() {
        return perguntar(new GetMissoesServicoQuery(null, null, true));
    }

    @PostMapping("missoes-servico")
    @Operation(summary = "Registar uma missão de serviço (pelo RH), de um ou mais participantes")
    @ApiResponse(responseCode = "201", description = "Registada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> registarMissaoServico(@RequestBody MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(null, "PEDIR", request, false));
    }

    @PatchMapping("missoes-servico/{missaoId}/autorizar")
    @Operation(summary = "Autorizar (despacho): os dias de ajudas de custo vão ao diário de factos")
    @ApiResponse(responseCode = "200", description = "Autorizada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> autorizarMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "AUTORIZAR", request, false));
    }

    @PatchMapping("missoes-servico/{missaoId}/recusar")
    @Operation(summary = "Recusar, com motivo")
    @ApiResponse(responseCode = "200", description = "Recusada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> recusarMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "RECUSAR", request, false));
    }

    @PatchMapping("missoes-servico/{missaoId}/regresso")
    @Operation(summary = "Registar o regresso: relatório e horas reais (os dias acertam-se)")
    @ApiResponse(responseCode = "200", description = "Realizada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> regressoMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "REGRESSO", request, false));
    }

    @PatchMapping("missoes-servico/{missaoId}/cancelar")
    @Operation(summary = "Cancelar, com motivo")
    @ApiResponse(responseCode = "200", description = "Cancelada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> cancelarMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "CANCELAR", request, false));
    }

    @PostMapping("me/missoes-servico")
    @Operation(summary = "Pedir uma missão para mim ou para a minha equipa directa")
    @ApiResponse(responseCode = "201", description = "Pedida",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> pedirMinhaMissaoServico(@RequestBody MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(null, "PEDIR", request, true));
    }

    @PatchMapping("me/missoes-servico/{missaoId}/autorizar")
    @Operation(summary = "A chefia directa de todos os participantes autoriza")
    @ApiResponse(responseCode = "200", description = "Autorizada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> autorizarMinhaEquipaMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "AUTORIZAR", request, true));
    }

    @PatchMapping("me/missoes-servico/{missaoId}/recusar")
    @Operation(summary = "A chefia directa de todos os participantes recusa")
    @ApiResponse(responseCode = "200", description = "Recusada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> recusarMinhaEquipaMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "RECUSAR", request, true));
    }

    @PatchMapping("me/missoes-servico/{missaoId}/regresso")
    @Operation(summary = "Um participante regista o relatório de regresso")
    @ApiResponse(responseCode = "200", description = "Realizada",
            content = @Content(schema = @Schema(implementation = MissaoServicoDTO.class)))
    public ResponseEntity<MissaoServicoDTO> regressoMinhaMissaoServico(@PathVariable String missaoId, @RequestBody(required = false) MissaoServicoRequestDTO request) {
        return enviar(new AccaoMissaoServicoCommand(missaoId, "REGRESSO", request, true));
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
