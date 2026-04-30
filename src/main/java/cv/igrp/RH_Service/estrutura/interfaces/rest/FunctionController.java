/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.estrutura.interfaces.rest;

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
import cv.igrp.RH_Service.estrutura.application.commands.*;
import cv.igrp.RH_Service.estrutura.application.queries.*;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaFunctionDTO;
import cv.igrp.RH_Service.estrutura.application.dto.FunctionResponse;
import cv.igrp.RH_Service.estrutura.application.dto.FunctionRequest;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/estrutura/functions")
@Tag(name = "Function", description = "Gestão de funções")
public class FunctionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FunctionController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar funções",
        description = "Retorna a lista paginada de funções",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaFunctionDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaFunctionDTO> getFunctions(
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new GetFunctionsQuery(active, pagina, tamanho);
        ResponseEntity<WrapperListaFunctionDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{functionId}")
    @Operation(
        summary = "Obter função por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FunctionResponse.class)
                )
            )
        }
    )
    public ResponseEntity<FunctionResponse> getFunctionById(
        @PathVariable(value = "functionId") String functionId) {

        LOGGER.debug("Operation started");

        final var query = new GetFunctionByIdQuery(functionId);
        ResponseEntity<FunctionResponse> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar função",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createFunction(
        @Valid @RequestBody FunctionRequest createFunctionRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateFunctionCommand(createFunctionRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{functionId}")
    @Operation(
        summary = "Atualizar função",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FunctionResponse.class)
                )
            )
        }
    )
    public ResponseEntity<FunctionResponse> updateFunction(
        @Valid @RequestBody FunctionRequest updateFunctionRequest,
        @PathVariable(value = "functionId") String functionId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateFunctionCommand(updateFunctionRequest, functionId);
        ResponseEntity<FunctionResponse> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{functionId}/deactivate")
    @Operation(
        summary = "Desativar função",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deactivateFunction(
        @PathVariable(value = "functionId") String functionId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarFunctionCommand(functionId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{functionId}/activate")
    @Operation(
        summary = "Ativar função",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateFunction(
        @PathVariable(value = "functionId") String functionId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarFunctionCommand(functionId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
