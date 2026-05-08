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
@RestController("colabsDependenteController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/dependentes")
@Tag(name = "Dependente", description = "Gestão de dependentes de funcionários")
public class DependenteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependenteController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DependenteController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar dependentes do funcionário")
    public ResponseEntity<WrapperListaDependenteDTO> getDependentesByFuncionario(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDependenteDTO> response = queryBus.handle(new GetDependentesByFuncionarioQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Criar dependente")
    public ResponseEntity<Map<String, ?>> createDependente(
            @PathVariable String funcionarioId,
            @Valid @RequestBody DependenteRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateDependenteCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{dependenteId}")
    @Operation(summary = "Obter dependente por ID")
    public ResponseEntity<DependenteResponseDTO> getDependenteById(
            @PathVariable String funcionarioId,
            @PathVariable String dependenteId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DependenteResponseDTO> response = queryBus.handle(new GetDependenteByIdQuery(dependenteId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{dependenteId}")
    @Operation(summary = "Actualizar dependente")
    public ResponseEntity<DependenteResponseDTO> updateDependente(
            @PathVariable String funcionarioId,
            @Valid @RequestBody DependenteRequestDTO request,
            @PathVariable String dependenteId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DependenteResponseDTO> response = commandBus.send(new UpdateDependenteCommand(request, dependenteId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{dependenteId}")
    @Operation(summary = "Desactivar dependente (soft delete)")
    public ResponseEntity<Map<String, ?>> deactivateDependente(
            @PathVariable String funcionarioId,
            @PathVariable String dependenteId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarDependenteCommand(dependenteId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{dependenteId}/activate")
    @Operation(summary = "Reactivar dependente")
    public ResponseEntity<Map<String, ?>> activateDependente(
            @PathVariable String funcionarioId,
            @PathVariable String dependenteId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarDependenteCommand(dependenteId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
