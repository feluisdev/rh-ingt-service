/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoAcumulacaoFuncoesCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetAcumulacoesFuncoesQuery;
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
@RestController("colabsAcumulacaoFuncoesController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "AcumulacaoFuncoes", description = "Acumulação de funções (Lei n.º 20/X/2023, arts. 20.º–24.º): pedido, autorização por despacho, cessação e caducidade")
public class AcumulacaoFuncoesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcumulacaoFuncoesController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public AcumulacaoFuncoesController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("acumulacoes-funcoes")
    @Operation(summary = "As acumulações de funções (filtro: estado)")
    @ApiResponse(responseCode = "200", description = "Acumulações",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<List<AcumulacaoFuncoesDTO>> getAcumulacoesFuncoes(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetAcumulacoesFuncoesQuery(estado, null, false));
    }

    @GetMapping("funcionarios/{funcionarioId}/acumulacoes-funcoes")
    @Operation(summary = "As acumulações de funções do colaborador")
    @ApiResponse(responseCode = "200", description = "Acumulações",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<List<AcumulacaoFuncoesDTO>> getAcumulacoesFuncoesColaborador(@PathVariable String funcionarioId) {
        return perguntar(new GetAcumulacoesFuncoesQuery(null, funcionarioId, false));
    }

    @PostMapping("funcionarios/{funcionarioId}/acumulacoes-funcoes")
    @Operation(summary = "Registar um pedido de acumulação de funções (pelo RH)")
    @ApiResponse(responseCode = "201", description = "Pedida",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> pedirAcumulacaoFuncoes(@PathVariable String funcionarioId, @RequestBody AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(funcionarioId, null, "PEDIR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acumulacoes-funcoes/{acumulacaoId}/autorizar")
    @Operation(summary = "Autorizar (despacho do dirigente máximo, ou dos membros do Governo se remunerada — art. 23.º)")
    @ApiResponse(responseCode = "200", description = "Autorizada",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> autorizarAcumulacaoFuncoes(@PathVariable String funcionarioId, @PathVariable String acumulacaoId,
                                                                          @RequestBody AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(funcionarioId, acumulacaoId, "AUTORIZAR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acumulacoes-funcoes/{acumulacaoId}/indeferir")
    @Operation(summary = "Indeferir, com motivo")
    @ApiResponse(responseCode = "200", description = "Indeferida",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> indeferirAcumulacaoFuncoes(@PathVariable String funcionarioId, @PathVariable String acumulacaoId,
                                                                          @RequestBody AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(funcionarioId, acumulacaoId, "INDEFERIR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/acumulacoes-funcoes/{acumulacaoId}/cessar")
    @Operation(summary = "Cessar a acumulação antes do fim")
    @ApiResponse(responseCode = "200", description = "Cessada",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> cessarAcumulacaoFuncoes(@PathVariable String funcionarioId, @PathVariable String acumulacaoId,
                                                                       @RequestBody(required = false) AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(funcionarioId, acumulacaoId, "CESSAR", request, false));
    }

    @GetMapping("me/acumulacoes-funcoes")
    @Operation(summary = "As minhas acumulações de funções")
    @ApiResponse(responseCode = "200", description = "Acumulações",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<List<AcumulacaoFuncoesDTO>> getMinhasAcumulacoesFuncoes() {
        return perguntar(new GetAcumulacoesFuncoesQuery(null, null, true));
    }

    @PostMapping("me/acumulacoes-funcoes")
    @Operation(summary = "Pedir autorização para acumular funções")
    @ApiResponse(responseCode = "201", description = "Pedida",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> pedirMinhaAcumulacaoFuncoes(@RequestBody AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(null, null, "PEDIR", request, true));
    }

    @PatchMapping("me/acumulacoes-funcoes/{acumulacaoId}/cessar")
    @Operation(summary = "Cessar a minha acumulação")
    @ApiResponse(responseCode = "200", description = "Cessada",
            content = @Content(schema = @Schema(implementation = AcumulacaoFuncoesDTO.class)))
    public ResponseEntity<AcumulacaoFuncoesDTO> cessarMinhaAcumulacaoFuncoes(@PathVariable String acumulacaoId,
                                                                            @RequestBody(required = false) AcumulacaoFuncoesRequestDTO request) {
        return enviar(new AccaoAcumulacaoFuncoesCommand(null, acumulacaoId, "CESSAR", request, true));
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
