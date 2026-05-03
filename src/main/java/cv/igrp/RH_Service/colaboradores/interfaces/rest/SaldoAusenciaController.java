/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.*;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@IgrpController
@RestController("colabsSaldoAusenciaController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia")
@Tag(name = "SaldoAusencia", description = "Gestão de saldos de ausência de funcionários")
public class SaldoAusenciaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaldoAusenciaController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public SaldoAusenciaController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar saldo de ausência")
    public ResponseEntity<Map<String, ?>> create(
            @PathVariable String funcionarioId,
            @Valid @RequestBody SaldoAusenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateSaldoAusenciaCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar saldos de ausência do funcionário")
    public ResponseEntity<WrapperListaSaldoAusenciaDTO> getAll(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) UUID tipoAusenciaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaSaldoAusenciaDTO> response = queryBus.handle(
                new GetSaldosByFuncionarioQuery(funcionarioId, ano, tipoAusenciaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{saldoId}")
    @Operation(summary = "Obter saldo de ausência por ID")
    public ResponseEntity<SaldoAusenciaResponseDTO> getById(
            @PathVariable String funcionarioId,
            @PathVariable String saldoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SaldoAusenciaResponseDTO> response = queryBus.handle(
                new GetSaldoAusenciaByIdQuery(funcionarioId, saldoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{saldoId}")
    @Operation(summary = "Actualizar dias de direito do saldo")
    public ResponseEntity<Map<String, ?>> update(
            @PathVariable String funcionarioId,
            @PathVariable String saldoId,
            @RequestParam Integer diasDireito) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new UpdateSaldoAusenciaCommand(funcionarioId, saldoId, diasDireito));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
