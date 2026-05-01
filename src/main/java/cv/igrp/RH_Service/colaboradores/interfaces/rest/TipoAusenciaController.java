package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.TipoAusenciaRequest;
import cv.igrp.RH_Service.colaboradores.application.dto.TipoAusenciaResponse;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaTipoAusenciaDTO;
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

@IgrpController
@RestController("colabsTipoAusenciaController")
@RequestMapping(path = "api/v1/rh/parametrizacoes/tipos-ausencia")
@Tag(name = "TipoAusencia", description = "Gestão de tipos de ausência")
public class TipoAusenciaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TipoAusenciaController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public TipoAusenciaController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar tipo de ausência")
    public ResponseEntity<Map<String, ?>> create(@Valid @RequestBody TipoAusenciaRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateTipoAusenciaCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar tipos de ausência")
    public ResponseEntity<WrapperListaTipoAusenciaDTO> getAll(@RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaTipoAusenciaDTO> response = queryBus.handle(new GetTiposAusenciaQuery(active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{id}")
    @Operation(summary = "Obter tipo de ausência por ID")
    public ResponseEntity<TipoAusenciaResponse> getById(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<TipoAusenciaResponse> response = queryBus.handle(new GetTipoAusenciaByIdQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{id}")
    @Operation(summary = "Actualizar tipo de ausência")
    public ResponseEntity<Map<String, ?>> update(@Valid @RequestBody TipoAusenciaRequest request, @PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new UpdateTipoAusenciaCommand(request, id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/ativar")
    @Operation(summary = "Activar tipo de ausência")
    public ResponseEntity<Map<String, ?>> ativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarTipoAusenciaCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/desativar")
    @Operation(summary = "Desactivar tipo de ausência")
    public ResponseEntity<Map<String, ?>> desativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarTipoAusenciaCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
