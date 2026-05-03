/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.colaboradores.application.queries.GetColaboradoresAuditHistoryQuery;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaAuditHistoryDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/colaboradores/audit")
@Tag(name = "ColaboradoresAuditHistory", description = "Consulta do historial de alterações nos dados de colaboradores")
public class ColaboradoresAuditHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColaboradoresAuditHistoryController.class);

    private final QueryBus queryBus;

    public ColaboradoresAuditHistoryController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping("{catalog}/{entityId}")
    @Operation(
        summary = "Historial de alterações de um registo de colaboradores",
        description = "Devolve todas as revisões de um registo identificado por catalog e entityId. " +
                      "Valores aceites para catalog: funcionarios, enquadramentos, contratos, dependentes, qualificacoes.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaAuditHistoryDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Catálogo desconhecido"),
            @ApiResponse(responseCode = "404", description = "Entidade não encontrada ou sem histórico")
        }
    )
    public ResponseEntity<WrapperListaAuditHistoryDTO> getColaboradoresAuditHistory(
        @PathVariable("catalog") String catalog,
        @PathVariable("entityId") String entityId) {

        LOGGER.debug("Operation started");

        final var query = new GetColaboradoresAuditHistoryQuery(catalog, entityId);
        ResponseEntity<WrapperListaAuditHistoryDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
