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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import cv.igrp.RH_Service.colaboradores.application.commands.SuspenderFeriasCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.SuspensaoFeriasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.SuspensaoFeriasRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaCriadoResponseDTO;

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
    public ResponseEntity<PedidoAusenciaCriadoResponseDTO> create(
            @PathVariable String funcionarioId,
            @Valid @RequestBody PedidoAusenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<PedidoAusenciaCriadoResponseDTO> response = commandBus.send(new CreatePedidoAusenciaCommand(funcionarioId, request));
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
    public ResponseEntity<PedidoAusenciaResponseDTO> getById(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<PedidoAusenciaResponseDTO> response = queryBus.handle(
                new GetPedidoAusenciaByIdQuery(funcionarioId, pedidoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/aprovar")
    @Operation(summary = "Aprovar pedido de ausência")
    public ResponseEntity<SuccessResponseDTO> aprovar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @Valid @RequestBody AprovarPedidoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new AprovarPedidoAusenciaCommand(funcionarioId, pedidoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/rejeitar")
    @Operation(summary = "Rejeitar pedido de ausência")
    public ResponseEntity<SuccessResponseDTO> rejeitar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @Valid @RequestBody RejeitarPedidoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new RejeitarPedidoAusenciaCommand(funcionarioId, pedidoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/cancelar")
    @Operation(summary = "Cancelar pedido de ausência (apenas o próprio funcionário)")
    public ResponseEntity<SuccessResponseDTO> cancelar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new CancelarPedidoAusenciaCommand(funcionarioId, pedidoId, funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/suspender")
    @Operation(summary = "Suspender ferias a partir de uma data (art. 8.o: parentalidade, doenca, "
            + "assistencia a familiares ou razoes imperiosas de servico). Devolve os dias ao saldo")
    @ApiResponse(responseCode = "200", description = "Ferias suspensas",
            content = @Content(schema = @Schema(implementation = SuspensaoFeriasResponseDTO.class)))
    public ResponseEntity<SuspensaoFeriasResponseDTO> suspender(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @RequestBody SuspensaoFeriasRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuspensaoFeriasResponseDTO> response = commandBus.send(
                new SuspenderFeriasCommand(funcionarioId, pedidoId, request.getData(), request.getMotivo()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{pedidoId}/terminar")
    @Operation(summary = "Terminar antes do fim um pedido em horas aprovado (ex.: amamentacao); o periodo acaba na vespera")
    @ApiResponse(responseCode = "200", description = "Pedido terminado",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponseDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponseDTO> terminar(
            @PathVariable String funcionarioId,
            @PathVariable String pedidoId,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.TerminarPedidoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponseDTO> response = commandBus.send(
                new cv.igrp.RH_Service.colaboradores.application.commands.TerminarPedidoAusenciaCommand(funcionarioId, pedidoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
