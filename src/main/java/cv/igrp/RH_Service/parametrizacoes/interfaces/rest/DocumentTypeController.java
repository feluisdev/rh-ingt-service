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
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaDocumentTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeRequestDTO;

import java.util.Map;
import java.util.List;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;

@IgrpController
@RestController
@RequestMapping(path = "api/v1/rh/catalogs/document-types")
@Tag(name = "DocumentType", description = "Gestão de tipos de documento")
public class DocumentTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTypeController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DocumentTypeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(
        summary = "Listar tipos de documento",
        description = "Retorna a lista paginada de tipos de documento",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista obtida com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaDocumentTypeDTO.class))
            )
        }
    )
    public ResponseEntity<WrapperListaDocumentTypeDTO> getDocumentTypes(
        @RequestParam(value = "codigo", required = false) String codigo,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "pagina", defaultValue = "0") String pagina,
        @RequestParam(value = "tamanho", defaultValue = "20") String tamanho) {

        LOGGER.debug("Operation started");
        final var query = new ListDocumentTypesQuery(codigo, active, pagina, tamanho);
        ResponseEntity<WrapperListaDocumentTypeDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @GetMapping(value = "{documentTypeId}")
    @Operation(
        summary = "Obter tipo de documento por ID",
        description = "Retorna os detalhes de um tipo de documento pelo seu identificador",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de documento encontrado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DocumentTypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<DocumentTypeResponseDTO> getDocumentTypeById(
        @PathVariable(value = "documentTypeId") String documentTypeId) {

        LOGGER.debug("Operation started");
        final var query = new GetDocumentTypeQuery(documentTypeId);
        ResponseEntity<DocumentTypeResponseDTO> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PostMapping
    @Operation(
        summary = "Criar tipo de documento",
        description = "Cria um novo tipo de documento",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Tipo de documento criado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> createDocumentType(
        @Valid @RequestBody DocumentTypeRequestDTO createDocumentTypeRequest) {

        LOGGER.debug("Operation started");
        final var command = new CreateDocumentTypeCommand(createDocumentTypeRequest);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PutMapping(value = "{documentTypeId}")
    @Operation(
        summary = "Actualizar tipo de documento",
        description = "Actualiza os dados de um tipo de documento existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de documento actualizado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DocumentTypeResponseDTO.class))
            )
        }
    )
    public ResponseEntity<DocumentTypeResponseDTO> updateDocumentType(
        @Valid @RequestBody DocumentTypeRequestDTO updateDocumentTypeRequest,
        @PathVariable(value = "documentTypeId") String documentTypeId) {

        LOGGER.debug("Operation started");
        final var command = new UpdateDocumentTypeCommand(updateDocumentTypeRequest, documentTypeId);
        ResponseEntity<DocumentTypeResponseDTO> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @DeleteMapping(value = "{documentTypeId}")
    @Operation(
        summary = "Desactivar tipo de documento",
        description = "Desactiva um tipo de documento existente",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de documento desactivado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> deleteDocumentType(
        @PathVariable(value = "documentTypeId") String documentTypeId) {

        LOGGER.debug("Operation started");
        final var command = new DesativarDocumentTypeCommand(documentTypeId);
        ResponseEntity<Map<String, ?>> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }

    @PatchMapping(value = "{documentTypeId}/activate")
    @Operation(
        summary = "Activar tipo de documento",
        description = "Reactiva um tipo de documento previamente desactivado",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Tipo de documento activado com sucesso",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))
            )
        }
    )
    public ResponseEntity<Map<String, ?>> activateDocumentType(
        @PathVariable(value = "documentTypeId") String documentTypeId) {

        LOGGER.debug("Operation started");
        final var command = new AtivarDocumentTypeCommand(documentTypeId);
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
        final var query = new GetDocumentTypesComboboxQuery();
        ResponseEntity<List<ComboboxItemDTO>> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(response.getBody());
    }
}