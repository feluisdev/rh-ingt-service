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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaLeaveTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeRequestDTO;

import java.util.Map;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/leave-types")
@Tag(name = "LeaveType", description = "Gestão de tipos de licença")
public class LeaveTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaveTypeController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public LeaveTypeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar tipos de licença",
        description = "Retorna a lista paginada de tipos de licença",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista obtida com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaLeaveTypeDTO.class))
            )
        }
    )
    public ResponseEntity<WrapperListaLeaveTypeDTO> getLeaveTypes(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");
        final var query = new ListLeaveTypesQuery(code, active, pagina, tamanho);
        ResponseEntity<WrapperListaLeaveTypeDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping(value = "{leaveTypeId}")
    @Operation(
        summary = "Obter tipo de licença por ID",
        description = "Retorna os detalhes de um tipo de licença pelo seu identificador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de licença encontrado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LeaveTypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<LeaveTypeResponseDTO> getLeaveTypeById(
        @PathVariable(value = "leaveTypeId") String leaveTypeId) {

        LOGGER.debug("Operation started");
        final var query = new GetLeaveTypeQuery(leaveTypeId);
        ResponseEntity<LeaveTypeResponseDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar tipo de licença",
        description = "Cria um novo tipo de licença",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Tipo de licença criado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createLeaveType(
        @Valid @RequestBody LeaveTypeRequestDTO createLeaveTypeRequest) {

        LOGGER.debug("Operation started");
        final var command = new CreateLeaveTypeCommand(createLeaveTypeRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping(value = "{leaveTypeId}")
    @Operation(
        summary = "Actualizar tipo de licença",
        description = "Actualiza os dados de um tipo de licença existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de licença actualizado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LeaveTypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<LeaveTypeResponseDTO> updateLeaveType(
        @Valid @RequestBody LeaveTypeRequestDTO updateLeaveTypeRequest,
        @PathVariable(value = "leaveTypeId") String leaveTypeId) {

        LOGGER.debug("Operation started");
        final var command = new UpdateLeaveTypeCommand(updateLeaveTypeRequest, leaveTypeId);
        ResponseEntity<LeaveTypeResponseDTO> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping(value = "{leaveTypeId}")
    @Operation(
        summary = "Desactivar tipo de licença",
        description = "Desactiva um tipo de licença existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de licença desactivado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteLeaveType(
        @PathVariable(value = "leaveTypeId") String leaveTypeId) {

        LOGGER.debug("Operation started");
        final var command = new DesativarLeaveTypeCommand(leaveTypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping(value = "{leaveTypeId}/activate")
    @Operation(
        summary = "Activar tipo de licença",
        description = "Reactiva um tipo de licença previamente desactivado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de licença activado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateLeaveType(
        @PathVariable(value = "leaveTypeId") String leaveTypeId) {

        LOGGER.debug("Operation started");
        final var command = new AtivarLeaveTypeCommand(leaveTypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
