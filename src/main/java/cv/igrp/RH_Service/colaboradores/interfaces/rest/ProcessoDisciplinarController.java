/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.dto.*;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
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

@IgrpController
@RestController("colabsProcessoDisciplinarController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares")
@Tag(name = "Processos Disciplinares", description = "Gestão de processos disciplinares do funcionário")
public class ProcessoDisciplinarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessoDisciplinarController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ProcessoDisciplinarController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar processos disciplinares do funcionário")
    public ResponseEntity<WrapperListaProcessoDisciplinarDTO> listarProcessos(
            @PathVariable String funcionarioId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaProcessoDisciplinarDTO> response = queryBus.handle(
                new GetProcessosDisciplinaresByFuncionarioQuery(funcionarioId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{processoId}")
    @Operation(summary = "Obter processo disciplinar por ID")
    public ResponseEntity<ProcessoDisciplinarDTO> getProcessoById(
            @PathVariable String funcionarioId,
            @PathVariable String processoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ProcessoDisciplinarDTO> response = queryBus.handle(
                new GetProcessoDisciplinarQuery(funcionarioId, processoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Registar novo processo disciplinar")
    public ResponseEntity<Map<String, ?>> criarProcesso(
            @PathVariable String funcionarioId,
            @Valid @RequestBody CriarProcessoDisciplinarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new CriarProcessoDisciplinarCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{processoId}")
    @Operation(summary = "Actualizar processo disciplinar")
    public ResponseEntity<Map<String, ?>> atualizarProcesso(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @Valid @RequestBody AtualizarProcessoDisciplinarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new AtualizarProcessoDisciplinarCommand(funcionarioId, processoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("{processoId}/documentos")
    @Operation(summary = "Associar documento a um processo disciplinar")
    public ResponseEntity<DocumentoUploadResponseDTO> registarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new RegistarDocumentoProcessoCommand(
                        funcionarioId, processoId,
                        request.getDocumentTypeId(), request.getFileKey(),
                        request.getOriginalFilename(), request.getContentType(),
                        request.getFileSize(), request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{processoId}/documentos")
    @Operation(summary = "Listar documentos de um processo disciplinar")
    public ResponseEntity<WrapperListaDocumentoDTO> listarDocumentos(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @RequestParam(required = false) java.util.UUID documentTypeId,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(
                new GetDocumentosSubRecursoQuery("PROCESSO_DISCIPLINAR", java.util.UUID.fromString(processoId), documentTypeId, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{processoId}/documentos/{docId}")
    @Operation(summary = "Obter documento de um processo por ID")
    public ResponseEntity<DocumentoResponseDTO> getDocumentoById(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(new GetDocumentoByIdQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{processoId}/documentos/{docId}/download")
    @Operation(summary = "Obter URL de download de um documento do processo")
    public ResponseEntity<DocumentoDownloadResponseDTO> downloadDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoDownloadResponseDTO> response = queryBus.handle(new GetDocumentoDownloadUrlQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{processoId}/documentos/{docId}")
    @Operation(summary = "Desactivar documento de um processo disciplinar")
    public ResponseEntity<Map<String, ?>> desativarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String processoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarDocumentoCommand(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
