package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriadoRequest;
import cv.igrp.RH_Service.colaboradores.application.dto.FeriadoResponse;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFeriadoDTO;
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
@RestController("colabsFeriadoController")
@RequestMapping(path = "api/v1/rh/parametrizacoes/feriados")
@Tag(name = "Feriado", description = "Gestão de feriados nacionais e municipais")
public class FeriadoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeriadoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FeriadoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar feriado")
    public ResponseEntity<Map<String, ?>> create(@Valid @RequestBody FeriadoRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateFeriadoCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar feriados")
    public ResponseEntity<WrapperListaFeriadoDTO> getAll(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Boolean isNational,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaFeriadoDTO> response = queryBus.handle(new GetFeriadosQuery(ano, isNational, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{id}")
    @Operation(summary = "Obter feriado por ID")
    public ResponseEntity<FeriadoResponse> getById(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<FeriadoResponse> response = queryBus.handle(new GetFeriadoByIdQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{id}")
    @Operation(summary = "Actualizar feriado")
    public ResponseEntity<Map<String, ?>> update(@Valid @RequestBody FeriadoRequest request, @PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new UpdateFeriadoCommand(request, id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/ativar")
    @Operation(summary = "Activar feriado")
    public ResponseEntity<Map<String, ?>> ativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarFeriadoCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{id}/desativar")
    @Operation(summary = "Desactivar feriado")
    public ResponseEntity<Map<String, ?>> desativar(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarFeriadoCommand(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
