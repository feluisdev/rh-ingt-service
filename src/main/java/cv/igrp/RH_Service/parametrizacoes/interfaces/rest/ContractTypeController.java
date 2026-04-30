/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

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
import cv.igrp.RH_Service.parametrizacoes.application.commands.*;
import cv.igrp.RH_Service.parametrizacoes.application.queries.*;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaContractTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/contract-types")
@Tag(name = "ContractType", description = "Gestão de tipos de contrato")
public class ContractTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractTypeController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ContractTypeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar tipos de contrato",
        description = "Retorna a lista paginada de tipos de contrato",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaContractTypeDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaContractTypeDTO> listContractTypes(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");

        final var query = new ListContractTypesQuery(code, isActive, pagina, tamanho);
        ResponseEntity<WrapperListaContractTypeDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{contractTypeId}")
    @Operation(
        summary = "Obter tipo de contrato por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractTypeResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ContractTypeResponseDTO> getContractTypeById(
        @PathVariable(value = "contractTypeId") String contractTypeId) {

        LOGGER.debug("Operation started");

        final var query = new GetContractTypeQuery(contractTypeId);
        ResponseEntity<ContractTypeResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar tipo de contrato",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createContractType(
        @Valid @RequestBody ContractTypeRequestDTO createContractTypeRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateContractTypeCommand(createContractTypeRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{contractTypeId}")
    @Operation(
        summary = "Atualizar tipo de contrato",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContractTypeResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<ContractTypeResponseDTO> updateContractType(
        @Valid @RequestBody ContractTypeRequestDTO updateContractTypeRequest,
        @PathVariable(value = "contractTypeId") String contractTypeId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateContractTypeCommand(updateContractTypeRequest, contractTypeId);
        ResponseEntity<ContractTypeResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{contractTypeId}")
    @Operation(
        summary = "Desativar tipo de contrato",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteContractType(
        @PathVariable(value = "contractTypeId") String contractTypeId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarContractTypeCommand(contractTypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{contractTypeId}/activate")
    @Operation(
        summary = "Ativar tipo de contrato",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateContractType(
        @PathVariable(value = "contractTypeId") String contractTypeId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarContractTypeCommand(contractTypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
