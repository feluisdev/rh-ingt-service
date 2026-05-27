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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaVinculoLaboralDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController("paramVinculoLaboralController")
@RequestMapping(path = "api/v1/rh/catalogs/vinculos-laborais")
@Tag(name = "VinculoLaboral", description = "Gestão de vínculos laborais")
public class VinculoLaboralController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VinculoLaboralController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public VinculoLaboralController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar vínculos laborais",
        description = "Retorna a lista paginada de vínculos laborais",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaVinculoLaboralDTO.class)
                )
            )
        }
    )
    public ResponseEntity<WrapperListaVinculoLaboralDTO> listVinculosLaborais(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "isActive", required = false) Boolean isActive,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho,
        @RequestParam(value = "nome", required = false) String nome) {

        LOGGER.debug("Operation started");

        final var query = new ListVinculosLaboraisQuery(code, isActive, pagina, tamanho, nome);
        ResponseEntity<WrapperListaVinculoLaboralDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping("{vinculoLaboralId}")
    @Operation(
        summary = "Obter vínculo laboral por ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VinculoLaboralResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<VinculoLaboralResponseDTO> getVinculoLaboralById(
        @PathVariable(value = "vinculoLaboralId") String vinculoLaboralId) {

        LOGGER.debug("Operation started");

        final var query = new GetVinculoLaboralQuery(vinculoLaboralId);
        ResponseEntity<VinculoLaboralResponseDTO> response = queryBus.handle(query);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar vínculo laboral",
        responses = {
            @ApiResponse(
                responseCode = "201",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createVinculoLaboral(
        @Valid @RequestBody VinculoLaboralRequestDTO createVinculoLaboralRequest) {

        LOGGER.debug("Operation started");

        final var command = new CreateVinculoLaboralCommand(createVinculoLaboralRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping("{vinculoLaboralId}")
    @Operation(
        summary = "Atualizar vínculo laboral",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VinculoLaboralResponseDTO.class)
                )
            )
        }
    )
    public ResponseEntity<VinculoLaboralResponseDTO> updateVinculoLaboral(
        @Valid @RequestBody VinculoLaboralRequestDTO updateVinculoLaboralRequest,
        @PathVariable(value = "vinculoLaboralId") String vinculoLaboralId) {

        LOGGER.debug("Operation started");

        final var command = new UpdateVinculoLaboralCommand(updateVinculoLaboralRequest, vinculoLaboralId);
        ResponseEntity<VinculoLaboralResponseDTO> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping("{vinculoLaboralId}")
    @Operation(
        summary = "Desativar vínculo laboral",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteVinculoLaboral(
        @PathVariable(value = "vinculoLaboralId") String vinculoLaboralId) {

        LOGGER.debug("Operation started");

        final var command = new DesativarVinculoLaboralCommand(vinculoLaboralId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);

        LOGGER.debug("Operation finished");

        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping("{vinculoLaboralId}/activate")
    @Operation(
        summary = "Ativar vínculo laboral",
        responses = {
            @ApiResponse(
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateVinculoLaboral(
        @PathVariable(value = "vinculoLaboralId") String vinculoLaboralId) {

        LOGGER.debug("Operation started");

        final var command = new AtivarVinculoLaboralCommand(vinculoLaboralId);
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
        final var query = new GetVinculosLaboraisComboboxQuery();
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}