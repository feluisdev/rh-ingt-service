/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
import cv.igrp.RH_Service.colaboradores.application.dto.*;

import java.util.Map;

@IgrpController
@RestController("colabsContratoController")
@RequestMapping(path = "api/v1/rh/contratos")
@Tag(name = "Contrato", description = "Gestão de contratos laborais")
public class ContratoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ContratoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar contrato")
    public ResponseEntity<Map<String, ?>> createContrato(@Valid @RequestBody ContratoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateContratoCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{contratoId}")
    @Operation(summary = "Obter contrato por ID")
    public ResponseEntity<ContratoResponseDTO> getContratoById(@PathVariable String contratoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ContratoResponseDTO> response = queryBus.handle(new GetContratoByIdQuery(contratoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{contratoId}")
    @Operation(summary = "Actualizar contrato")
    public ResponseEntity<ContratoResponseDTO> updateContrato(@Valid @RequestBody ContratoRequestDTO request, @PathVariable String contratoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ContratoResponseDTO> response = commandBus.send(new UpdateContratoCommand(request, contratoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{contratoId}")
    @Operation(summary = "Desactivar contrato (soft delete)")
    public ResponseEntity<Map<String, ?>> deactivateContrato(@PathVariable String contratoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarContratoCommand(contratoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{contratoId}/activate")
    @Operation(summary = "Reactivar contrato")
    public ResponseEntity<Map<String, ?>> activateContrato(@PathVariable String contratoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarContratoCommand(contratoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
