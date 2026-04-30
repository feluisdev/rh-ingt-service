package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

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
import cv.igrp.RH_Service.parametrizacoes.application.queries.GetAuditHistoryQuery;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaAuditHistoryDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/audit")
@Tag(name = "AuditHistory", description = "Consulta do historial de alterações nos catálogos de parametrização")
public class AuditHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditHistoryController.class);

    private final QueryBus queryBus;

    public AuditHistoryController(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @GetMapping("{catalog}/{entityId}")
    @Operation(
        summary = "Historial de alterações de uma entrada de catálogo",
        description = "Devolve todas as revisões de uma entrada identificada por catalog e entityId. " +
                      "Valores aceites para catalog: reference-options, worker-states, professional-situations, " +
                      "contract-types, document-types, leave-types, leave-mobility-subtypes, public-holidays.",
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
    public ResponseEntity<WrapperListaAuditHistoryDTO> getAuditHistory(
        @PathVariable("catalog") String catalog,
        @PathVariable("entityId") String entityId) {

        LOGGER.debug("Operation started");

        final var query = new GetAuditHistoryQuery(catalog, entityId);
        ResponseEntity<WrapperListaAuditHistoryDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
