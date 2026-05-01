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
@RestController
@RequestMapping(path = "api/v1/rh/enquadramentos")
@Tag(name = "Enquadramento", description = "Gestão de enquadramentos profissionais")
public class EnquadramentoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnquadramentoController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public EnquadramentoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar enquadramento profissional")
    public ResponseEntity<Map<String, ?>> createEnquadramento(
            @Valid @RequestBody EnquadramentoRequest request) {
        LOGGER.debug("Operation started");
        final var command = new CreateEnquadramentoCommand(request);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{enquadramentoId}")
    @Operation(summary = "Obter enquadramento por ID")
    public ResponseEntity<EnquadramentoResponse> getEnquadramentoById(
            @PathVariable(value = "enquadramentoId") String enquadramentoId) {
        LOGGER.debug("Operation started");
        final var query = new GetEnquadramentoByIdQuery(enquadramentoId);
        ResponseEntity<EnquadramentoResponse> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
