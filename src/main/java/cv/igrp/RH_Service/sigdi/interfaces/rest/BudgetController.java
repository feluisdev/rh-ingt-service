/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.queries.*;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/budget")
@Tag(
    name = "Sigdi",
    description = "gest"
)
public class BudgetController {

  
  private final QueryBus queryBus;

  public BudgetController(QueryBus queryBus) {
          this.queryBus = queryBus;
          
  }
   @GetMapping(
   value = "availability"
  )
  @Operation(
    summary = "Get budget",
    description = "Get budget",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = BudgetInfoDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<BudgetInfoDTO> getBudget(
    @RequestParam(value = "economicClassifier") String economicClassifier)
  {

      final var query = new GetBudgetQuery(economicClassifier);

      return queryBus.handle(query);

  }

}