/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;
import cv.igrp.RH_Service.sigdi.application.dto.DashboardSummaryResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/dashboard")
@Tag(
    name = "SIGDI-Dashboard",
    description = "Executive dashboard summary"
)
public class DashboardController {

  private final QueryBus queryBus;

  public DashboardController(QueryBus queryBus) {
    this.queryBus = queryBus;
  }

  @GetMapping(value = "summary")
  @Operation(
    summary = "Get dashboard summary",
    description = "Retorna o resumo executivo do dashboard (execução orçamental, orçamento disponível, OKRs em risco, pendências)",
    responses = {
      @ApiResponse(
          responseCode = "200",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DashboardSummaryResponseDTO.class, type = "object")
          )
      )
    }
  )
  public ResponseEntity<DashboardSummaryResponseDTO> getDashboardSummary()
  {
    final var query = new GetDashboardSummaryQuery();
    return queryBus.handle(query);
  }
}
