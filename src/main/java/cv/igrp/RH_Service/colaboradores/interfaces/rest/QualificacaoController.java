/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.framework.stereotype.IgrpController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.colaboradores.application.commands.*;
import cv.igrp.RH_Service.colaboradores.application.queries.*;
import cv.igrp.RH_Service.colaboradores.application.dto.*;

import java.util.Map;

@IgrpController
@RestController("colabsQualificacaoController")
@RequestMapping(path = "api/v1/rh/qualificacoes")
@Tag(name = "Qualificação", description = "Gestão de qualificações académicas de funcionários")
public class QualificacaoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QualificacaoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public QualificacaoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Criar qualificação")
    public ResponseEntity<Map<String, ?>> createQualificacao(@Valid @RequestBody QualificacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateQualificacaoCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{qualificacaoId}")
    @Operation(summary = "Obter qualificação por ID")
    public ResponseEntity<QualificacaoResponseDTO> getQualificacaoById(@PathVariable String qualificacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<QualificacaoResponseDTO> response = queryBus.handle(new GetQualificacaoByIdQuery(qualificacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{qualificacaoId}")
    @Operation(summary = "Actualizar qualificação")
    public ResponseEntity<QualificacaoResponseDTO> updateQualificacao(@Valid @RequestBody QualificacaoRequestDTO request, @PathVariable String qualificacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<QualificacaoResponseDTO> response = commandBus.send(new UpdateQualificacaoCommand(request, qualificacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{qualificacaoId}")
    @Operation(summary = "Desactivar qualificação (soft delete)")
    public ResponseEntity<Map<String, ?>> deactivateQualificacao(@PathVariable String qualificacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarQualificacaoCommand(qualificacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{qualificacaoId}/activate")
    @Operation(summary = "Reactivar qualificação")
    public ResponseEntity<Map<String, ?>> activateQualificacao(@PathVariable String qualificacaoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarQualificacaoCommand(qualificacaoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("{qualificacaoId}/documentos")
    @Operation(summary = "Associar documento a uma qualificação")
    public ResponseEntity<DocumentoUploadResponseDTO> registarDocumento(
            @PathVariable String qualificacaoId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new RegistarDocumentoQualificacaoCommand(
                        qualificacaoId,
                        request.getDocumentTypeId(), request.getFileKey(),
                        request.getOriginalFilename(), request.getContentType(),
                        request.getFileSize(), request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
