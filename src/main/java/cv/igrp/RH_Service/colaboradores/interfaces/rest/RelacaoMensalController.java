/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.dto.RelacaoMensalDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetRelacaoMensalCsvQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetRelacaoMensalQuery;
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
@RestController("colabsRelacaoMensalController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "RelacaoMensal", description = "Relação mensal de assiduidade de cada serviço: faltas e licenças de cada funcionário e sua natureza (DL n.º 3/2010, art. 75.º); só leitura")
public class RelacaoMensalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelacaoMensalController.class);
    private final QueryBus queryBus;

    public RelacaoMensalController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping("assiduidade/relacao-mensal")
    @Operation(summary = "Relação mensal de um serviço (unidade e, por omissão, subunidades): por colaborador, férias, faltas por natureza, faltas por justificar, licenças, trabalho suplementar e pendências")
    @ApiResponse(responseCode = "200", description = "Relação mensal",
            content = @Content(schema = @Schema(implementation = RelacaoMensalDTO.class)))
    public ResponseEntity<RelacaoMensalDTO> getRelacaoMensal(
            @RequestParam(value = "mes") String mes,
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<RelacaoMensalDTO> response = queryBus.handle(new GetRelacaoMensalQuery(mes, unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping(value = "assiduidade/relacao-mensal.csv", produces = "text/csv")
    @Operation(summary = "A mesma relação mensal em CSV (separador ;, UTF-8 com BOM), para a folha de cálculo")
    @ApiResponse(responseCode = "200", description = "Ficheiro CSV")
    public ResponseEntity<byte[]> getRelacaoMensalCsv(
            @RequestParam(value = "mes") String mes,
            @RequestParam(value = "unidadeId") String unidadeId,
            @RequestParam(value = "incluirSubunidades", required = false, defaultValue = "true") Boolean incluirSubunidades) {
        LOGGER.debug("Operation started");
        ResponseEntity<byte[]> response = queryBus.handle(new GetRelacaoMensalCsvQuery(mes, unidadeId, incluirSubunidades));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
