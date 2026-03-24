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
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.RH_Service.sigdi.application.commands.*;
import cv.igrp.RH_Service.sigdi.application.dto.CreateIdentityRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;

@IgrpController
@RestController
@RequestMapping(path = "ap1/v1strategy")
@Tag(
    name = "Sigdi",
    description = "gest strategies"
)
public class StrategyController {

  
  private final CommandBus commandBus;

  public StrategyController(CommandBus commandBus) {
          
          this.commandBus = commandBus;
  }
   @PostMapping(
   value = "identities"
  )
  @Operation(
    summary = "Create identitie",
    description = "Create identitie",
    responses = {
      @ApiResponse(
          responseCode = "201",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = IdentityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<IdentityResponseDTO> createIdentitie(@Valid @RequestBody CreateIdentityRequestDTO createIdentitieRequest
    )
  {

      final var command = new CreateIdentitieCommand(createIdentitieRequest);

      return commandBus.send(command);

  }

   @PutMapping(
   value = "identities/{id}/activate"
  )
  @Operation(
    summary = "Activate identitie",
    description = "Activate identitie",
    responses = {
      @ApiResponse(
          responseCode = "200",
          
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(
                  implementation = IdentityResponseDTO.class,
                  type = "object")
          )
      )
    }
  )
  
  public ResponseEntity<IdentityResponseDTO> activateIdentitie(
    @PathVariable(value = "id") String id)
  {

      final var command = new ActivateIdentitieCommand(id);

      return commandBus.send(command);

  }

}