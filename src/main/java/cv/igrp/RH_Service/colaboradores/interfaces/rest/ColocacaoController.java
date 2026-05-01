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

@IgrpController
@RestController("colabsColocacaoController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/colocacoes")
@Tag(name = "Colocações", description = "Gestão de colocações de funcionários em unidades orgânicas")
public class ColocacaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColocacaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ColocacaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Registar nova colocação (fecha a anterior automaticamente)")
    public ResponseEntity<Map<String, ?>> registarColocacao(
            @PathVariable String funcionarioId,
            @Valid @RequestBody RegistarColocacaoRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new RegistarColocacaoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar histórico de colocações do funcionário")
    public ResponseEntity<WrapperListaColocacaoDTO> listarColocacoes(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Boolean current) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaColocacaoDTO> response = queryBus.handle(
                new GetColocacoesByFuncionarioQuery(funcionarioId, current));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("atual")
    @Operation(summary = "Obter colocação actual do funcionário")
    public ResponseEntity<ColocacaoResponse> getColocacaoAtual(@PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ColocacaoResponse> response = queryBus.handle(new GetColocacaoAtualQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{colocacaoId}")
    @Operation(summary = "Obter colocação por ID")
    public ResponseEntity<ColocacaoResponse> getColocacaoById(
            @PathVariable String funcionarioId,
            @PathVariable String colocacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ColocacaoResponse> response = queryBus.handle(
                new GetColocacaoByIdQuery(funcionarioId, colocacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{colocacaoId}")
    @Operation(summary = "Corrigir dados de uma colocação (notas e/ou data de fim)")
    public ResponseEntity<ColocacaoResponse> atualizarColocacao(
            @PathVariable String funcionarioId,
            @PathVariable String colocacaoId,
            @Valid @RequestBody AtualizarColocacaoRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<ColocacaoResponse> response = commandBus.send(
                new AtualizarColocacaoCommand(funcionarioId, colocacaoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{colocacaoId}")
    @Operation(summary = "Remover colocação (soft delete — apenas se não for a actual)")
    public ResponseEntity<Map<String, ?>> desativarColocacao(
            @PathVariable String funcionarioId,
            @PathVariable String colocacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new DesativarColocacaoCommand(funcionarioId, colocacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
