/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoListaAntiguidadeCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.AccaoReclamacaoAntiguidadeCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.AprovarListaAntiguidadeCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.ReclamarListaAntiguidadeCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ReclamacaoAntiguidadeRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetListaAntiguidadeOficialQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetListasAntiguidadeOficiaisQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetMinhasListasAntiguidadeQuery;
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
@RestController("colabsListaAntiguidadeController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "ListaAntiguidade", description = "O ciclo da lista de antiguidade (DL n.º 3/2010, arts. 69.º–74.º): aprovar e congelar, afixar, reclamações e recursos, lista definitiva e publicação no Boletim Oficial")
public class ListaAntiguidadeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListaAntiguidadeController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ListaAntiguidadeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("listas-antiguidade")
    @Operation(summary = "Aprovar a lista de antiguidade de um serviço num ano: a lista gerada congela-se")
    @ApiResponse(responseCode = "201", description = "Lista aprovada",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> aprovarListaAntiguidade(@RequestBody ListaAntiguidadeRequestDTO request) {
        return enviar(new AprovarListaAntiguidadeCommand(request));
    }

    @GetMapping("listas-antiguidade")
    @Operation(summary = "As listas de antiguidade oficiais (cabeçalho), filtradas por ano e serviço")
    @ApiResponse(responseCode = "200", description = "Listas",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<List<ListaAntiguidadeOficialDTO>> getListasAntiguidade(
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "unidadeId", required = false) String unidadeId) {
        return perguntar(new GetListasAntiguidadeOficiaisQuery(ano, unidadeId));
    }

    @GetMapping("listas-antiguidade/{listaId}")
    @Operation(summary = "Uma lista oficial com as linhas congeladas e as reclamações")
    @ApiResponse(responseCode = "200", description = "Lista",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> getListaAntiguidade(@PathVariable String listaId) {
        return perguntar(new GetListaAntiguidadeOficialQuery(listaId));
    }

    @PatchMapping("listas-antiguidade/{listaId}/afixar")
    @Operation(summary = "Afixar (data e local): abre o prazo de reclamação e avisa cada pessoa da lista")
    @ApiResponse(responseCode = "200", description = "Afixada",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> afixarListaAntiguidade(@PathVariable String listaId,
                                                                             @RequestBody ListaAntiguidadeRequestDTO request) {
        return enviar(new AccaoListaAntiguidadeCommand(listaId, "AFIXAR", request));
    }

    @PatchMapping("listas-antiguidade/{listaId}/recalcular")
    @Operation(summary = "Voltar a gerar as linhas (depois de corrigidos os dados de uma reclamação deferida)")
    @ApiResponse(responseCode = "200", description = "Recalculada",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> recalcularListaAntiguidade(@PathVariable String listaId) {
        return enviar(new AccaoListaAntiguidadeCommand(listaId, "RECALCULAR", null));
    }

    @PatchMapping("listas-antiguidade/{listaId}/definitiva")
    @Operation(summary = "Tornar a lista definitiva: prazo de reclamação esgotado e reclamações decididas")
    @ApiResponse(responseCode = "200", description = "Definitiva",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> tornarDefinitivaListaAntiguidade(@PathVariable String listaId) {
        return enviar(new AccaoListaAntiguidadeCommand(listaId, "DEFINITIVA", null));
    }

    @PatchMapping("listas-antiguidade/{listaId}/publicar")
    @Operation(summary = "Registar a publicação no Boletim Oficial (série, número e data); até 30 de Abril")
    @ApiResponse(responseCode = "200", description = "Publicada",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> publicarListaAntiguidade(@PathVariable String listaId,
                                                                               @RequestBody ListaAntiguidadeRequestDTO request) {
        return enviar(new AccaoListaAntiguidadeCommand(listaId, "PUBLICAR", request));
    }

    @PatchMapping("listas-antiguidade/{listaId}/anular")
    @Operation(summary = "Anular a lista (antes de publicada), com motivo")
    @ApiResponse(responseCode = "200", description = "Anulada",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<ListaAntiguidadeOficialDTO> anularListaAntiguidade(@PathVariable String listaId,
                                                                             @RequestBody ListaAntiguidadeRequestDTO request) {
        return enviar(new AccaoListaAntiguidadeCommand(listaId, "ANULAR", request));
    }

    @PostMapping("listas-antiguidade/{listaId}/reclamacoes")
    @Operation(summary = "Registar uma reclamação (RH), no prazo, com um dos fundamentos da lei")
    @ApiResponse(responseCode = "201", description = "Reclamação registada",
            content = @Content(schema = @Schema(implementation = ReclamacaoAntiguidadeDTO.class)))
    public ResponseEntity<ReclamacaoAntiguidadeDTO> reclamarListaAntiguidade(@PathVariable String listaId,
                                                                             @RequestBody ReclamacaoAntiguidadeRequestDTO request) {
        return enviar(new ReclamarListaAntiguidadeCommand(false, listaId, request));
    }

    @PatchMapping("listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/decidir")
    @Operation(summary = "Decidir a reclamação (dirigente): deferida ou indeferida, fundamentada")
    @ApiResponse(responseCode = "200", description = "Decidida",
            content = @Content(schema = @Schema(implementation = ReclamacaoAntiguidadeDTO.class)))
    public ResponseEntity<ReclamacaoAntiguidadeDTO> decidirReclamacaoAntiguidade(@PathVariable String listaId,
            @PathVariable String reclamacaoId, @RequestBody ReclamacaoAntiguidadeRequestDTO request) {
        return enviar(new AccaoReclamacaoAntiguidadeCommand(listaId, reclamacaoId, "DECIDIR", request));
    }

    @PatchMapping("listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/recurso")
    @Operation(summary = "Registar o recurso da decisão, no prazo (20 dias; 60 no estrangeiro)")
    @ApiResponse(responseCode = "200", description = "Recurso registado",
            content = @Content(schema = @Schema(implementation = ReclamacaoAntiguidadeDTO.class)))
    public ResponseEntity<ReclamacaoAntiguidadeDTO> recorrerReclamacaoAntiguidade(@PathVariable String listaId,
            @PathVariable String reclamacaoId, @RequestBody ReclamacaoAntiguidadeRequestDTO request) {
        return enviar(new AccaoReclamacaoAntiguidadeCommand(listaId, reclamacaoId, "RECORRER", request));
    }

    @PatchMapping("listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/recurso/decidir")
    @Operation(summary = "Registar a decisão do recurso (membro do Governo)")
    @ApiResponse(responseCode = "200", description = "Recurso decidido",
            content = @Content(schema = @Schema(implementation = ReclamacaoAntiguidadeDTO.class)))
    public ResponseEntity<ReclamacaoAntiguidadeDTO> decidirRecursoAntiguidade(@PathVariable String listaId,
            @PathVariable String reclamacaoId, @RequestBody ReclamacaoAntiguidadeRequestDTO request) {
        return enviar(new AccaoReclamacaoAntiguidadeCommand(listaId, reclamacaoId, "DECIDIR_RECURSO", request));
    }

    @GetMapping("me/listas-antiguidade")
    @Operation(summary = "As listas afixadas, definitivas ou publicadas onde apareço, com a minha linha e as minhas reclamações")
    @ApiResponse(responseCode = "200", description = "Listas",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeOficialDTO.class)))
    public ResponseEntity<List<ListaAntiguidadeOficialDTO>> getMinhasListasAntiguidade() {
        return perguntar(new GetMinhasListasAntiguidadeQuery());
    }

    @PostMapping("me/listas-antiguidade/{listaId}/reclamacoes")
    @Operation(summary = "Reclamar da lista de antiguidade (o próprio), no prazo")
    @ApiResponse(responseCode = "201", description = "Reclamação registada",
            content = @Content(schema = @Schema(implementation = ReclamacaoAntiguidadeDTO.class)))
    public ResponseEntity<ReclamacaoAntiguidadeDTO> reclamarMinhaListaAntiguidade(@PathVariable String listaId,
                                                                                  @RequestBody ReclamacaoAntiguidadeRequestDTO request) {
        return enviar(new ReclamarListaAntiguidadeCommand(true, listaId, request));
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
