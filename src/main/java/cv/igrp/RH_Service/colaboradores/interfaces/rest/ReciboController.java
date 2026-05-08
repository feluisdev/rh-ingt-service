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
@RestController("colabsReciboController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/recibos")
@Tag(name = "Recibos de Vencimento", description = "Gestão de recibos de vencimento dos funcionários")
public class ReciboController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReciboController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ReciboController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @GetMapping
    @Operation(summary = "Listar recibos de vencimento do funcionário")
    public ResponseEntity<WrapperListaReciboDTO> listarRecibos(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Integer periodYear,
            @RequestParam(required = false) Integer periodMonth) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaReciboDTO> response = queryBus.handle(
                new GetRecibosByFuncionarioQuery(funcionarioId, periodYear, periodMonth));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{reciboId}")
    @Operation(summary = "Obter recibo por ID")
    public ResponseEntity<ReciboVencimentoDTO> getReciboById(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId) {
        LOGGER.debug("Operation started");
        ResponseEntity<ReciboVencimentoDTO> response = queryBus.handle(new GetReciboVencimentoQuery(reciboId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping
    @Operation(summary = "Emitir recibo de vencimento")
    public ResponseEntity<Map<String, ?>> criarRecibo(
            @PathVariable String funcionarioId,
            @Valid @RequestBody CriarReciboRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new CriarReciboVencimentoCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("{reciboId}/documentos")
    @Operation(summary = "Associar documento a um recibo de vencimento")
    public ResponseEntity<DocumentoUploadResponseDTO> registarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new RegistarDocumentoReciboCommand(
                        funcionarioId, reciboId,
                        request.getDocumentTypeId(), request.getFileKey(),
                        request.getOriginalFilename(), request.getContentType(),
                        request.getFileSize(), request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{reciboId}/documentos")
    @Operation(summary = "Listar documentos de um recibo de vencimento")
    public ResponseEntity<WrapperListaDocumentoDTO> listarDocumentos(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId,
            @RequestParam(required = false) java.util.UUID documentTypeId,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(
                new GetDocumentosSubRecursoQuery("RECIBO_VENCIMENTO", java.util.UUID.fromString(reciboId), documentTypeId, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{reciboId}/documentos/{docId}")
    @Operation(summary = "Obter documento de um recibo por ID")
    public ResponseEntity<DocumentoResponseDTO> getDocumentoById(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(new GetDocumentoByIdQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{reciboId}/documentos/{docId}/download")
    @Operation(summary = "Obter URL de download de um documento do recibo")
    public ResponseEntity<DocumentoDownloadResponseDTO> downloadDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoDownloadResponseDTO> response = queryBus.handle(new GetDocumentoDownloadUrlQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{reciboId}/documentos/{docId}")
    @Operation(summary = "Desactivar documento de um recibo de vencimento")
    public ResponseEntity<Map<String, ?>> desativarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String reciboId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarDocumentoCommand(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
