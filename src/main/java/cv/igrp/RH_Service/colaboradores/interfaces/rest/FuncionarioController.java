/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RestController("colabsFuncionarioController")
@RequestMapping(path = "api/v1/rh/funcionarios")
@Tag(name = "Funcionario", description = "Gestão de funcionários")
public class FuncionarioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FuncionarioController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FuncionarioController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar funcionários")
    public ResponseEntity<WrapperListaFuncionarioDTO> getFuncionarios(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "nif", required = false) String nif,
            @RequestParam(value = "situacaoProfissional", required = false) String situacaoProfissional,
            @RequestParam(value = "unidadeOrganicaId", required = false) String unidadeOrganicaId,
            @RequestParam(value = "careerId", required = false) String careerId,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "pagina", defaultValue = "0") String pagina,
            @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {
        LOGGER.debug("Operation started");
        final var query = new GetFuncionariosQuery(nome, nif, situacaoProfissional, unidadeOrganicaId, careerId, active, pagina, tamanho);
        ResponseEntity<WrapperListaFuncionarioDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}")
    @Operation(summary = "Obter funcionário por ID")
    public ResponseEntity<FuncionarioResponseDTO> getFuncionarioById(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetFuncionarioByIdQuery(funcionarioId);
        ResponseEntity<FuncionarioResponseDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Criar funcionário")
    public ResponseEntity<Map<String, ?>> createFuncionario(
            @Valid @RequestBody FuncionarioRequestDTO request) {
        LOGGER.debug("Operation started");
        final var command = new CreateFuncionarioCommand(request);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{funcionarioId}")
    @Operation(summary = "Actualizar funcionário")
    public ResponseEntity<FuncionarioResponseDTO> updateFuncionario(
            @Valid @RequestBody FuncionarioRequestDTO request,
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var command = new UpdateFuncionarioCommand(request, funcionarioId);
        ResponseEntity<FuncionarioResponseDTO> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}/enquadramento")
    @Operation(summary = "Obter enquadramento actual do funcionário")
    public ResponseEntity<EnquadramentoResponseDTO> getEnquadramentoAtual(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetEnquadramentoAtualQuery(funcionarioId);
        ResponseEntity<EnquadramentoResponseDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}/enquadramentos/historico")
    @Operation(summary = "Listar histórico de enquadramentos do funcionário")
    public ResponseEntity<WrapperListaEnquadramentoDTO> getEnquadramentosHistorico(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetEnquadramentosHistoricoQuery(funcionarioId);
        ResponseEntity<WrapperListaEnquadramentoDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}/contratos")
    @Operation(summary = "Listar contratos do funcionário")
    public ResponseEntity<WrapperListaContratoDTO> getContratosByFuncionario(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetContratosByFuncionarioQuery(funcionarioId);
        ResponseEntity<WrapperListaContratoDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}/dependentes")
    @Operation(summary = "Listar dependentes do funcionário")
    public ResponseEntity<WrapperListaDependenteDTO> getDependentesByFuncionario(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetDependentesByFuncionarioQuery(funcionarioId);
        ResponseEntity<WrapperListaDependenteDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{funcionarioId}/qualificacoes")
    @Operation(summary = "Listar qualificações do funcionário")
    public ResponseEntity<WrapperListaQualificacaoDTO> getQualificacoesByFuncionario(
            @PathVariable(value = "funcionarioId") String funcionarioId) {
        LOGGER.debug("Operation started");
        final var query = new GetQualificacoesByFuncionarioQuery(funcionarioId);
        ResponseEntity<WrapperListaQualificacaoDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
