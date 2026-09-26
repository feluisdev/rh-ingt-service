/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AccaoFechoMensalCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.ExportacaoSalarialDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetExportacaoSalarialQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetFechosMensaisQuery;
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
@RestController("colabsFechoMensalController")
@RequestMapping(path = "api/v1/rh/salarial")
@Tag(name = "FechoMensal", description = "Fecho mensal e exportação para o processamento salarial (DL n.º 3/2010, art. 75.º): o RH envia os factos e a relação mensal; o salarial calcula remunerações e descontos")
public class FechoMensalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FechoMensalController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FechoMensalController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("fechos")
    @Operation(summary = "Os meses fechados (e reabertos), dos mais recentes")
    @ApiResponse(responseCode = "200", description = "Fechos",
            content = @Content(schema = @Schema(implementation = FechoMensalDTO.class)))
    public ResponseEntity<List<FechoMensalDTO>> getFechosMensais() {
        return perguntar(new GetFechosMensaisQuery());
    }

    @PatchMapping("fechos/{mes}/fechar")
    @Operation(summary = "Fechar o mês (AAAA-MM): congela a relação mensal; os factos com efeito nele passam ao mês seguinte")
    @ApiResponse(responseCode = "200", description = "Fechado",
            content = @Content(schema = @Schema(implementation = FechoMensalDTO.class)))
    public ResponseEntity<FechoMensalDTO> fecharMes(@PathVariable String mes) {
        return enviar(new AccaoFechoMensalCommand(mes, "FECHAR", null));
    }

    @PatchMapping("fechos/{mes}/reabrir")
    @Operation(summary = "Reabrir um mês fechado, com motivo")
    @ApiResponse(responseCode = "200", description = "Reaberto",
            content = @Content(schema = @Schema(implementation = FechoMensalDTO.class)))
    public ResponseEntity<FechoMensalDTO> reabrirMes(@PathVariable String mes, @RequestBody FechoMensalRequestDTO request) {
        return enviar(new AccaoFechoMensalCommand(mes, "REABRIR", request));
    }

    @GetMapping("exportacao")
    @Operation(summary = "A exportação de um mês para o salarial (contrato versionado): factos + relação mensal (congelada se fechado)")
    @ApiResponse(responseCode = "200", description = "Exportação",
            content = @Content(schema = @Schema(implementation = ExportacaoSalarialDTO.class)))
    public ResponseEntity<ExportacaoSalarialDTO> getExportacaoSalarial(@RequestParam(value = "mes") String mes) {
        return perguntar(new GetExportacaoSalarialQuery(mes));
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
