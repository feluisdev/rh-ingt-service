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
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaOrganizationalUnitDTO;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/estrutura/organizational-units")
@Tag(name = "OrganizationalUnit", description = "Gestão de unidades orgânicas hierárquicas (Direcção, Departamento, Divisão, Secção)")
public class OrganizationalUnitController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationalUnitController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public OrganizationalUnitController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar unidades orgânicas",
        description = "Retorna a lista paginada de unidades orgânicas",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaOrganizationalUnitDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaOrganizationalUnitDTO> getOrganizationalUnits(
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "parentUnitId", required = false) String parentUnitId,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");

        final var query = new GetOrganizationalUnitsQuery(active, parentUnitId, pagina, tamanho, code, nome);
        ResponseEntity<WrapperListaOrganizationalUnitDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{unitId}")
    @Operation(
        summary = "Obter unidade orgânica por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationalUnitResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<OrganizationalUnitResponseDTO> getOrganizationalUnitById(
        @PathVariable(value = "unitId") String unitId) {

        LOGGER.debug("Operation started");

        final var query = new GetOrganizationalUnitByIdQuery(unitId);
        ResponseEntity<OrganizationalUnitResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar unidade orgânica",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createOrganizationalUnit(
        @Valid @RequestBody OrganizationalUnitRequestDTO createOrganizationalUnitRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateOrganizationalUnitCommand(createOrganizationalUnitRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{unitId}")
    @Operation(
        summary = "Atualizar unidade orgânica",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationalUnitResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<OrganizationalUnitResponseDTO> updateOrganizationalUnit(
        @Valid @RequestBody OrganizationalUnitRequestDTO updateOrganizationalUnitRequest,
        @PathVariable(value = "unitId") String unitId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateOrganizationalUnitCommand(unitId, updateOrganizationalUnitRequest);
        ResponseEntity<OrganizationalUnitResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{unitId}/deactivate")
    @Operation(
        summary = "Desativar unidade orgânica (soft delete)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deactivateOrganizationalUnit(
        @PathVariable(value = "unitId") String unitId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarOrganizationalUnitCommand(unitId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{unitId}/activate")
    @Operation(
        summary = "Ativar unidade orgânica",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateOrganizationalUnit(
        @PathVariable(value = "unitId") String unitId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarOrganizationalUnitCommand(unitId);
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
    public ResponseEntity<List<ComboboxItemDTO>> getCombobox(
        @RequestParam(value = "parentUnitId", required = false) String parentUnitId) {
        LOGGER.debug("Operation started");
        final var query = new GetOrganizationalUnitsComboboxQuery(parentUnitId);
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}
