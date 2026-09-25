/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.dto.FactosSalariaisDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetFactosSalariaisCsvQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetFactosSalariaisQuery;
import cv.igrp.framework.core.domain.CommandBus;
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
@RestController("colabsSalarialController")
@RequestMapping(path = "api/v1/rh/salarial")
@Tag(name = "Salarial", description = "Fronteira com o processamento salarial: os factos do RH que a outra aplicação consome (admissões, movimentos, cessações…), por mês de processamento; sem valores")
public class SalarialController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalarialController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SalarialController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping("factos")
    @Operation(summary = "Os factos que entram num mês de processamento, pela ordem em que foram registados")
    @ApiResponse(responseCode = "200", description = "Factos do mês",
            content = @Content(schema = @Schema(implementation = FactosSalariaisDTO.class)))
    public ResponseEntity<FactosSalariaisDTO> getFactosSalariais(@RequestParam(value = "mes") String mes) {
        LOGGER.debug("Operation started");
        ResponseEntity<FactosSalariaisDTO> response = queryBus.handle(new GetFactosSalariaisQuery(mes, null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping(value = "factos.csv", produces = "text/csv")
    @Operation(summary = "Os mesmos factos em CSV (separador ;, UTF-8 com BOM)")
    @ApiResponse(responseCode = "200", description = "Ficheiro CSV")
    public ResponseEntity<byte[]> getFactosSalariaisCsv(@RequestParam(value = "mes") String mes) {
        LOGGER.debug("Operation started");
        ResponseEntity<byte[]> response = queryBus.handle(new GetFactosSalariaisCsvQuery(mes));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionarios/{funcionarioId}/factos")
    @Operation(summary = "Os factos de um colaborador, do mais recente para o mais antigo")
    @ApiResponse(responseCode = "200", description = "Factos do colaborador",
            content = @Content(schema = @Schema(implementation = FactosSalariaisDTO.class)))
    public ResponseEntity<FactosSalariaisDTO> getFactosDoFuncionario(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<FactosSalariaisDTO> response = queryBus.handle(new GetFactosSalariaisQuery(null, funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
