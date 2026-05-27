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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaLeaveMobilitySubtypeDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/leave-mobility-subtypes")
@Tag(name = "LeaveMobilitySubtype", description = "Gestão de subtipos de licença e mobilidade")
public class LeaveMobilitySubtypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaveMobilitySubtypeController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public LeaveMobilitySubtypeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar subtipos de licença/mobilidade",
        description = "Retorna a lista paginada de subtipos de licença e mobilidade",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista obtida com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaLeaveMobilitySubtypeDTO.class))
            )
        }
    )
    public ResponseEntity<WrapperListaLeaveMobilitySubtypeDTO> getLeaveMobilitySubtypes(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "recordType", required = false) String recordType,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");
        final var query = new ListLeaveMobilitySubtypesQuery(code, recordType, active, pagina, tamanho, nome);
        ResponseEntity<WrapperListaLeaveMobilitySubtypeDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping(value = "{leaveMobilitySubtypeId}")
    @Operation(
        summary = "Obter subtipo de licença/mobilidade por ID",
        description = "Retorna os detalhes de um subtipo pelo seu identificador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Subtipo encontrado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LeaveMobilitySubtypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<LeaveMobilitySubtypeResponseDTO> getLeaveMobilitySubtypeById(
        @PathVariable(value = "leaveMobilitySubtypeId") String leaveMobilitySubtypeId) {

        LOGGER.debug("Operation started");
        final var query = new GetLeaveMobilitySubtypeQuery(leaveMobilitySubtypeId);
        ResponseEntity<LeaveMobilitySubtypeResponseDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar subtipo de licença/mobilidade",
        description = "Cria um novo subtipo de licença ou mobilidade",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Subtipo criado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createLeaveMobilitySubtype(
        @Valid @RequestBody LeaveMobilitySubtypeRequestDTO createLeaveMobilitySubtypeRequest) {

        LOGGER.debug("Operation started");
        final var command = new CreateLeaveMobilitySubtypeCommand(createLeaveMobilitySubtypeRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping(value = "{leaveMobilitySubtypeId}")
    @Operation(
        summary = "Actualizar subtipo de licença/mobilidade",
        description = "Actualiza os dados de um subtipo existente (recordType é imutável)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Subtipo actualizado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LeaveMobilitySubtypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<LeaveMobilitySubtypeResponseDTO> updateLeaveMobilitySubtype(
        @Valid @RequestBody LeaveMobilitySubtypeRequestDTO updateLeaveMobilitySubtypeRequest,
        @PathVariable(value = "leaveMobilitySubtypeId") String leaveMobilitySubtypeId) {

        LOGGER.debug("Operation started");
        final var command = new UpdateLeaveMobilitySubtypeCommand(updateLeaveMobilitySubtypeRequest, leaveMobilitySubtypeId);
        ResponseEntity<LeaveMobilitySubtypeResponseDTO> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping(value = "{leaveMobilitySubtypeId}")
    @Operation(
        summary = "Desactivar subtipo de licença/mobilidade",
        description = "Desactiva um subtipo existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Subtipo desactivado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteLeaveMobilitySubtype(
        @PathVariable(value = "leaveMobilitySubtypeId") String leaveMobilitySubtypeId) {

        LOGGER.debug("Operation started");
        final var command = new DesativarLeaveMobilitySubtypeCommand(leaveMobilitySubtypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping(value = "{leaveMobilitySubtypeId}/activate")
    @Operation(
        summary = "Activar subtipo de licença/mobilidade",
        description = "Reactiva um subtipo previamente desactivado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Subtipo activado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateLeaveMobilitySubtype(
        @PathVariable(value = "leaveMobilitySubtypeId") String leaveMobilitySubtypeId) {

        LOGGER.debug("Operation started");
        final var command = new AtivarLeaveMobilitySubtypeCommand(leaveMobilitySubtypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("combobox")
    @Operation(
        summary = "Listar para combobox",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<List<ComboboxItemDTO>> getCombobox() {
        LOGGER.debug("Operation started");
        final var query = new GetLeaveMobilitySubtypesComboboxQuery();
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}