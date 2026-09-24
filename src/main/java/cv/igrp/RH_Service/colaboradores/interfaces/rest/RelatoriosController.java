/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetListaAntiguidadeCsvQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetListaAntiguidadeQuery;
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
@RestController("colabsRelatoriosController")
@RequestMapping(path = "api/v1/rh/relatorios")
@Tag(name = "Relatorios", description = "Relatórios de gestão de cada serviço: lista de antiguidade (DL n.º 3/2010, art. 69.º), mapa de efectivos e indicadores do pessoal (Lei n.º 20/X/2023); só leitura")
public class RelatoriosController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelatoriosController.class);
    private final QueryBus queryBus;

    public RelatoriosController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping("lista-antiguidade")
    @Operation(summary = "Lista de antiguidade de um serviço, com referência a 31 de Dezembro do ano anterior: por cargo e, em cada cargo, por antiguidade")
    @ApiResponse(responseCode = "200", description = "Lista de antiguidade",
            content = @Content(schema = @Schema(implementation = ListaAntiguidadeDTO.class)))
    public ResponseEntity<ListaAntiguidadeDTO> getListaAntiguidade(
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<ListaAntiguidadeDTO> response = queryBus.handle(new GetListaAntiguidadeQuery(ano, unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping(value = "lista-antiguidade.csv", produces = "text/csv")
    @Operation(summary = "A mesma lista de antiguidade em CSV (separador ;, UTF-8 com BOM), para afixar e publicar")
    @ApiResponse(responseCode = "200", description = "Ficheiro CSV")
    public ResponseEntity<byte[]> getListaAntiguidadeCsv(
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<byte[]> response = queryBus.handle(new GetListaAntiguidadeCsvQuery(ano, unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("mapa-efectivos")
    @Operation(summary = "Mapa de efectivos de um serviço (hoje): por unidade e cargo, os Lugares activos, providos, vagos e congelados")
    @ApiResponse(responseCode = "200", description = "Mapa de efectivos",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.MapaEfectivosDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.MapaEfectivosDTO> getMapaEfectivos(
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.MapaEfectivosDTO> response = queryBus.handle(
                new cv.igrp.RH_Service.colaboradores.application.queries.GetMapaEfectivosQuery(unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping(value = "mapa-efectivos.csv", produces = "text/csv")
    @Operation(summary = "O mesmo mapa de efectivos em CSV (separador ;, UTF-8 com BOM)")
    @ApiResponse(responseCode = "200", description = "Ficheiro CSV")
    public ResponseEntity<byte[]> getMapaEfectivosCsv(
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<byte[]> response = queryBus.handle(
                new cv.igrp.RH_Service.colaboradores.application.queries.GetMapaEfectivosCsvQuery(unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("indicadores")
    @Operation(summary = "Indicadores do pessoal de um serviço num ano (balanço social): efectivos por género, escalão etário, contrato, carreira e unidade; entradas, saídas, absentismo e horas extras")
    @ApiResponse(responseCode = "200", description = "Indicadores",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.IndicadoresPessoalDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.IndicadoresPessoalDTO> getIndicadoresPessoal(
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades,
            @RequestParam(value = "ano", required = false) Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.IndicadoresPessoalDTO> response = queryBus.handle(
                new cv.igrp.RH_Service.colaboradores.application.queries.GetIndicadoresPessoalQuery(unidadeId, incluirSubunidades, ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
