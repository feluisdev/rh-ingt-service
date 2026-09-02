/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
import cv.igrp.RH_Service.colaboradores.application.dto.AfectacaoRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/colaboradores/assignments")
@Tag(name = "Assignment", description = "Afectação de colaboradores a Lugares")
public class AssignmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public AssignmentController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(
        summary = "Afectar colaborador a um Lugar",
        responses = { @ApiResponse(responseCode = "201", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<Map<String, ?>> afectar(
        @Valid @RequestBody AfectacaoRequestDTO request) {

        LOGGER.debug("Operation started");
        final var command = new AfectarColaboradorCommand(request);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionario/{funcionarioId}/unidade-atual")
    @Operation(
        summary = "Unidade atual do colaborador",
        responses = { @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<Map<String, Object>> getUnidadeAtual(
        @PathVariable(value = "funcionarioId") String funcionarioId) {

        LOGGER.debug("Operation started");
        final var query = new GetUnidadeAtualQuery(funcionarioId);
        ResponseEntity<Map<String, Object>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("funcionario/{funcionarioId}/chefe")
    @Operation(
        summary = "Chefe do colaborador",
        responses = { @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<Map<String, Object>> getChefe(
        @PathVariable(value = "funcionarioId") String funcionarioId) {

        LOGGER.debug("Operation started");
        final var query = new GetChefeFuncionarioQuery(funcionarioId);
        ResponseEntity<Map<String, Object>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("unidade/{unidadeId}/responsavel")
    @Operation(
        summary = "Responsável da unidade",
        responses = { @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<Map<String, Object>> getResponsavel(
        @PathVariable(value = "unidadeId") String unidadeId) {

        LOGGER.debug("Operation started");
        final var query = new GetResponsavelUnidadeQuery(unidadeId);
        ResponseEntity<Map<String, Object>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("unidade/{unidadeId}/vagas")
    @Operation(
        summary = "Vagas da unidade",
        responses = { @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<Map<String, Object>> getVagas(
        @PathVariable(value = "unidadeId") String unidadeId) {

        LOGGER.debug("Operation started");
        final var query = new GetVagasUnidadeQuery(unidadeId);
        ResponseEntity<Map<String, Object>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("unidade/{unidadeId}/vagas/lista")
    @Operation(
        summary = "Lista de Lugares vagos da unidade (picker de admissão)",
        responses = { @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json")) }
    )
    public ResponseEntity<cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO> getVagasLista(
        @PathVariable(value = "unidadeId") String unidadeId) {

        LOGGER.debug("Operation started");
        final var query = new GetVagasListaUnidadeQuery(unidadeId);
        ResponseEntity<cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders()).body(response.getBody());
    }
}
