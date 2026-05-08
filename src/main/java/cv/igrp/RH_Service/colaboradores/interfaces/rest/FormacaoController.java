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
@RestController("colabsFormacaoController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/formacoes")
@Tag(name = "Formações Profissionais", description = "Gestão de formações profissionais do funcionário")
public class FormacaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormacaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public FormacaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar formações profissionais do funcionário")
    public ResponseEntity<WrapperListaFormacaoDTO> listarFormacoes(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Integer year) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaFormacaoDTO> response = queryBus.handle(
                new GetFormacoesByFuncionarioQuery(funcionarioId, year));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{formacaoId}")
    @Operation(summary = "Obter formação por ID")
    public ResponseEntity<FormacaoDTO> getFormacaoById(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<FormacaoDTO> response = queryBus.handle(
                new GetFormacaoQuery(funcionarioId, formacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Registar nova formação profissional")
    public ResponseEntity<Map<String, ?>> criarFormacao(
            @PathVariable String funcionarioId,
            @Valid @RequestBody CriarFormacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new CriarFormacaoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{formacaoId}")
    @Operation(summary = "Actualizar formação profissional")
    public ResponseEntity<Map<String, ?>> atualizarFormacao(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @Valid @RequestBody AtualizarFormacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new AtualizarFormacaoCommand(funcionarioId, formacaoId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{formacaoId}")
    @Operation(summary = "Remover formação profissional")
    public ResponseEntity<Map<String, ?>> removerFormacao(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new RemoverFormacaoCommand(funcionarioId, formacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Documentos ───────────────────────────────────────────────────────────

    @PostMapping("{formacaoId}/documentos")
    @Operation(summary = "Associar documento a uma formação")
    public ResponseEntity<DocumentoUploadResponseDTO> registarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new RegistarDocumentoFormacaoCommand(
                        funcionarioId, formacaoId,
                        request.getDocumentTypeId(), request.getFileKey(),
                        request.getOriginalFilename(), request.getContentType(),
                        request.getFileSize(), request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{formacaoId}/documentos")
    @Operation(summary = "Listar documentos de uma formação")
    public ResponseEntity<WrapperListaDocumentoDTO> listarDocumentos(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @RequestParam(required = false) java.util.UUID documentTypeId,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(
                new GetDocumentosSubRecursoQuery("FORMACAO", java.util.UUID.fromString(formacaoId), documentTypeId, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{formacaoId}/documentos/{docId}")
    @Operation(summary = "Obter documento de uma formação por ID")
    public ResponseEntity<DocumentoResponseDTO> getDocumentoById(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(
                new GetDocumentoByIdQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{formacaoId}/documentos/{docId}/download")
    @Operation(summary = "Obter URL de download de um documento de formação")
    public ResponseEntity<DocumentoDownloadResponseDTO> downloadDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoDownloadResponseDTO> response = queryBus.handle(
                new GetDocumentoDownloadUrlQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{formacaoId}/documentos/{docId}")
    @Operation(summary = "Desactivar documento de uma formação")
    public ResponseEntity<Map<String, ?>> desativarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String formacaoId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new DesativarDocumentoCommand(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
