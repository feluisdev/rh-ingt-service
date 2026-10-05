/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.CancelarReservaLugarCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.CancelarReservaLugarRequestDTO;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandBus;
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
@RestController("colabsReservaLugarController")
@RequestMapping(path = "api/v1/rh/funcionarios")
@Tag(name = "ReservaLugar", description = "Lugar reservado para quem foi registado sem contrato: fica ocupado quando o contrato for registado")
public class ReservaLugarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservaLugarController.class);
    private final CommandBus commandBus;

    public ReservaLugarController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @PatchMapping("{funcionarioId}/reserva-lugar/cancelar")
    @Operation(summary = "Cancelar o Lugar reservado do colaborador, com motivo; o Lugar fica livre para outros")
    @ApiResponse(responseCode = "200", description = "Cancelada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> cancelarReservaLugar(@PathVariable String funcionarioId,
                                                                   @RequestBody CancelarReservaLugarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new CancelarReservaLugarCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
