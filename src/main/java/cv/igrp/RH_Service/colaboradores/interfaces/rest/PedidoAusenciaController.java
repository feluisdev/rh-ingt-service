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
@RestController("colabsPedidoAusenciaController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia")
@Tag(name = "PedidoAusencia", description = "Gestão de pedidos de ausência de funcionários")
public class PedidoAusenciaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PedidoAusenciaController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public PedidoAusenciaController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar pedido de ausência")
    public ResponseEntity<Map<String, ?>> create(
            @PathVariable String funcionarioId,
            @Valid @RequestBody PedidoAusenciaRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreatePedidoAusenciaCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar pedidos de ausência do funcionário")
    public ResponseEntity<WrapperListaPedidoAusenciaDTO> getAll(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID tipoAusenciaId,
            @RequestParam(required = false) Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaPedidoAusenciaDTO> response = queryBus.handle(
                new GetPedidosByFuncionarioQuery(funcionarioId, estado, tipoAusenciaId, ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{pedidoId}")
    @Operation(summary = "Obter pedido de ausência por ID")
    public ResponseEntity<PedidoAusenciaResponse> getById(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<PedidoAusenciaResponse> response = queryBus.handle(
                new GetPedidoAusenciaByIdQuery(funcionarioId, pedidoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/aprovar")
    @Operation(summary = "Aprovar pedido de ausência")
    public ResponseEntity<Map<String, ?>> aprovar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @Valid @RequestBody AprovarPedidoRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new AprovarPedidoAusenciaCommand(funcionarioId, pedidoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/rejeitar")
    @Operation(summary = "Rejeitar pedido de ausência")
    public ResponseEntity<Map<String, ?>> rejeitar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @Valid @RequestBody RejeitarPedidoRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new RejeitarPedidoAusenciaCommand(funcionarioId, pedidoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/cancelar")
    @Operation(summary = "Cancelar pedido de ausência (apenas o próprio funcionário)")
    public ResponseEntity<Map<String, ?>> cancelar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new CancelarPedidoAusenciaCommand(funcionarioId, pedidoId, funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
