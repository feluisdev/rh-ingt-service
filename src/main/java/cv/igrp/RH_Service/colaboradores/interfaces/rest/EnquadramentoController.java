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
@RestController("colabsEnquadramentoController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/enquadramentos")
@Tag(name = "Enquadramento", description = "Gestão de enquadramentos profissionais")
public class EnquadramentoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnquadramentoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public EnquadramentoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar histórico de enquadramentos do funcionário")
    public ResponseEntity<WrapperListaEnquadramentoDTO> getEnquadramentosHistorico(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaEnquadramentoDTO> response = queryBus.handle(new GetEnquadramentosHistoricoQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("atual")
    @Operation(summary = "Obter enquadramento actual do funcionário")
    public ResponseEntity<EnquadramentoResponseDTO> getEnquadramentoAtual(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<EnquadramentoResponseDTO> response = queryBus.handle(new GetEnquadramentoAtualQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Criar enquadramento profissional")
    public ResponseEntity<Map<String, ?>> createEnquadramento(
            @PathVariable String funcionarioId,
            @Valid @RequestBody EnquadramentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateEnquadramentoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{enquadramentoId}")
    @Operation(summary = "Obter enquadramento por ID")
    public ResponseEntity<EnquadramentoResponseDTO> getEnquadramentoById(
            @PathVariable String funcionarioId,
            @PathVariable String enquadramentoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<EnquadramentoResponseDTO> response = queryBus.handle(new GetEnquadramentoByIdQuery(enquadramentoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
