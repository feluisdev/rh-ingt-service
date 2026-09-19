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

import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;

@IgrpController
@RestController("colabsDadosBancariosController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/dados-bancarios")
@Tag(name = "Dados Bancários", description = "Gestão de dados bancários dos funcionários")
public class DadosBancariosController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DadosBancariosController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DadosBancariosController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Registar dados bancários")
    public ResponseEntity<SuccessResponseDTO> createDadosBancarios(
            @PathVariable String funcionarioId,
            @Valid @RequestBody DadosBancariosRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new CreateDadosBancariosCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar dados bancários do funcionário")
    public ResponseEntity<WrapperListaDadosBancariosDTO> getDadosBancariossByFuncionario(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDadosBancariosDTO> response = queryBus.handle(new GetDadosBancariossByFuncionarioQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{dadosBancariosId}")
    @Operation(summary = "Obter dados bancários por ID")
    public ResponseEntity<DadosBancariosResponseDTO> getDadosBancariosById(
            @PathVariable String funcionarioId,
            @PathVariable String dadosBancariosId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DadosBancariosResponseDTO> response = queryBus.handle(new GetDadosBancariossByIdQuery(dadosBancariosId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{dadosBancariosId}")
    @Operation(summary = "Actualizar dados bancários")
    public ResponseEntity<DadosBancariosResponseDTO> updateDadosBancarios(
            @PathVariable String funcionarioId,
            @Valid @RequestBody DadosBancariosRequestDTO request,
            @PathVariable String dadosBancariosId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DadosBancariosResponseDTO> response = commandBus.send(new UpdateDadosBancariosCommand(request, dadosBancariosId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{dadosBancariosId}")
    @Operation(summary = "Desactivar dados bancários (soft delete)")
    public ResponseEntity<SuccessResponseDTO> desativarDadosBancarios(
            @PathVariable String funcionarioId,
            @PathVariable String dadosBancariosId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new DesativarDadosBancariosCommand(dadosBancariosId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{dadosBancariosId}/activate")
    @Operation(summary = "Reactivar dados bancários")
    public ResponseEntity<SuccessResponseDTO> activateDadosBancarios(
            @PathVariable String funcionarioId,
            @PathVariable String dadosBancariosId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new AtivarDadosBancariosCommand(dadosBancariosId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
