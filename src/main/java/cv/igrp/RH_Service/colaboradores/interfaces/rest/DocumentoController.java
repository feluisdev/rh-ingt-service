/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.DesativarDocumentoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.UploadDocumentoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoDownloadResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoUploadResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.UploadDocumentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDocumentoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetDocumentoByIdQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetDocumentoDownloadUrlQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetDocumentosByFuncionarioQuery;
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
import java.util.UUID;

@IgrpController
@RestController("colabsDocumentoController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/documentos")
@Tag(name = "Documentos", description = "Gestão de documentos de funcionários")
public class DocumentoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentoController.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DocumentoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Registar metadados de documento após upload no MinIO")
    public ResponseEntity<DocumentoUploadResponseDTO> registar(
            @PathVariable String funcionarioId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new UploadDocumentoCommand(
                        funcionarioId,
                        request.getDocumentTypeId(),
                        request.getReferenceEntity(),
                        request.getReferenceId(),
                        request.getFileKey(),
                        request.getOriginalFilename(),
                        request.getContentType(),
                        request.getFileSize(),
                        request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar documentos de um funcionário")
    public ResponseEntity<WrapperListaDocumentoDTO> listar(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) UUID documentTypeId,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(
                new GetDocumentosByFuncionarioQuery(funcionarioId, documentTypeId, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{documentoId}")
    @Operation(summary = "Obter metadados de um documento")
    public ResponseEntity<DocumentoResponseDTO> getById(
            @PathVariable String funcionarioId,
            @PathVariable String documentoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(
                new GetDocumentoByIdQuery(funcionarioId, documentoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{documentoId}/download")
    @Operation(summary = "Obter URL pré-assinada para download (via MinIO)")
    public ResponseEntity<DocumentoDownloadResponseDTO> download(
            @PathVariable String funcionarioId,
            @PathVariable String documentoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoDownloadResponseDTO> response = queryBus.handle(
                new GetDocumentoDownloadUrlQuery(funcionarioId, documentoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{documentoId}")
    @Operation(summary = "Desactivar documento (soft delete)")
    public ResponseEntity<Map<String, ?>> desativar(
            @PathVariable String funcionarioId,
            @PathVariable String documentoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new DesativarDocumentoCommand(funcionarioId, documentoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
