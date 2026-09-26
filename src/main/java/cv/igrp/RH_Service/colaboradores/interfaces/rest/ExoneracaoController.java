/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoExoneracaoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetExoneracoesQuery;
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
@RestController("colabsExoneracaoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Exoneracao", description = "Exoneração voluntária (Lei n.º 20/X/2023, arts. 94.º e 95.º): pré-aviso de 60 dias, condicionantes (processo disciplinar, inquérito, garantia de formação), efeitos no máximo aos 90 dias, cessação do vínculo e publicação")
public class ExoneracaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExoneracaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ExoneracaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("exoneracoes")
    @Operation(summary = "Os pedidos de exoneração voluntária (filtro: estado), com as condicionantes de hoje")
    @ApiResponse(responseCode = "200", description = "Pedidos",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<List<ExoneracaoDTO>> getExoneracoes(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetExoneracoesQuery(estado, null, false));
    }

    @GetMapping("funcionarios/{funcionarioId}/exoneracoes")
    @Operation(summary = "Os pedidos de exoneração do colaborador")
    @ApiResponse(responseCode = "200", description = "Pedidos",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<List<ExoneracaoDTO>> getExoneracoesColaborador(@PathVariable String funcionarioId) {
        return perguntar(new GetExoneracoesQuery(null, funcionarioId, false));
    }

    @PostMapping("funcionarios/{funcionarioId}/exoneracoes")
    @Operation(summary = "Registar o pedido de exoneração (pelo RH), com o pré-aviso de 60 dias")
    @ApiResponse(responseCode = "201", description = "Registado",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<ExoneracaoDTO> pedirExoneracao(@PathVariable String funcionarioId, @RequestBody(required = false) ExoneracaoRequestDTO request) {
        return enviar(new AccaoExoneracaoCommand(funcionarioId, null, "PEDIR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/exoneracoes/{exoneracaoId}/deferir")
    @Operation(summary = "Deferir (despacho): produz efeitos no dia devido — já, se chegou")
    @ApiResponse(responseCode = "200", description = "Deferida",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<ExoneracaoDTO> deferirExoneracao(@PathVariable String funcionarioId, @PathVariable String exoneracaoId,
                                                          @RequestBody ExoneracaoRequestDTO request) {
        return enviar(new AccaoExoneracaoCommand(funcionarioId, exoneracaoId, "DEFERIR", request, false));
    }

    @PatchMapping("funcionarios/{funcionarioId}/exoneracoes/{exoneracaoId}/desistir")
    @Operation(summary = "Registar a desistência do pedido")
    @ApiResponse(responseCode = "200", description = "Desistida",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<ExoneracaoDTO> desistirExoneracao(@PathVariable String funcionarioId, @PathVariable String exoneracaoId) {
        return enviar(new AccaoExoneracaoCommand(funcionarioId, exoneracaoId, "DESISTIR", null, false));
    }

    @GetMapping("me/exoneracoes")
    @Operation(summary = "Os meus pedidos de exoneração")
    @ApiResponse(responseCode = "200", description = "Pedidos",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<List<ExoneracaoDTO>> getMinhasExoneracoes() {
        return perguntar(new GetExoneracoesQuery(null, null, true));
    }

    @PostMapping("me/exoneracoes")
    @Operation(summary = "Pedir a minha exoneração, com o pré-aviso de 60 dias")
    @ApiResponse(responseCode = "201", description = "Pedida",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<ExoneracaoDTO> pedirMinhaExoneracao(@RequestBody(required = false) ExoneracaoRequestDTO request) {
        return enviar(new AccaoExoneracaoCommand(null, null, "PEDIR", request, true));
    }

    @PatchMapping("me/exoneracoes/{exoneracaoId}/desistir")
    @Operation(summary = "Desistir do meu pedido de exoneração")
    @ApiResponse(responseCode = "200", description = "Desistida",
            content = @Content(schema = @Schema(implementation = ExoneracaoDTO.class)))
    public ResponseEntity<ExoneracaoDTO> desistirMinhaExoneracao(@PathVariable String exoneracaoId) {
        return enviar(new AccaoExoneracaoCommand(null, exoneracaoId, "DESISTIR", null, true));
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
