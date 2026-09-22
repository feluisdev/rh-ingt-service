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

import java.util.UUID;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.LicencaEfeitoResponseDTO;

@IgrpController
@RestController("colabsLicencaMobilidadeController")
@RequestMapping(path = "api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade")
@Tag(name = "LicençaMobilidade", description = "Gestão de licenças e mobilidade de funcionários")
public class LicencaMobilidadeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicencaMobilidadeController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public LicencaMobilidadeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping
    @Operation(summary = "Registar licença ou mobilidade")
    public ResponseEntity<SuccessResponseDTO> create(
            @PathVariable String funcionarioId,
            @Valid @RequestBody LicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new CreateLicencaMobilidadeCommand(funcionarioId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping
    @Operation(summary = "Listar licenças e mobilidades do funcionário")
    public ResponseEntity<WrapperListaLicencaMobilidadeDTO> getAll(
            @PathVariable String funcionarioId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID subtipoId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaLicencaMobilidadeDTO> response = queryBus.handle(
                new GetLicencasByFuncionarioQuery(funcionarioId, active, subtipoId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{licencaId}")
    @Operation(summary = "Obter licença/mobilidade por ID")
    public ResponseEntity<LicencaMobilidadeResponseDTO> getById(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<LicencaMobilidadeResponseDTO> response = queryBus.handle(
                new GetLicencaMobilidadeByIdQuery(funcionarioId, licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}")
    @Operation(summary = "Actualizar licença/mobilidade")
    public ResponseEntity<SuccessResponseDTO> update(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody LicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new UpdateLicencaMobilidadeCommand(funcionarioId, licencaId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Workflow ─────────────────────────────────────────────────────────────

    @PutMapping("{licencaId}/approve")
    @Operation(summary = "Deferir licença/mobilidade (PENDING → APPROVED). Os efeitos no Lugar aplicam-se na data de início, não na do despacho")
    public ResponseEntity<LicencaEfeitoResponseDTO> approve(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<LicencaEfeitoResponseDTO> response = commandBus.send(new AprovarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/reject")
    @Operation(summary = "Rejeitar licença/mobilidade (PENDING → REJECTED)")
    public ResponseEntity<SuccessResponseDTO> reject(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody RejeitarLicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new RejeitarLicencaMobilidadeCommand(licencaId, request.getRejectionReason()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/close")
    @Operation(summary = "Registar regresso antecipado (art. 46.º n.º 4): encurta o período para hoje. Recusa o que ainda não começou ou já terminou")
    public ResponseEntity<LicencaEfeitoResponseDTO> close(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<LicencaEfeitoResponseDTO> response = commandBus.send(new EncerrarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("{licencaId}/consolidar")
    @Operation(summary = "Consolidar a mobilidade transitória (art. 132.º n.º 4): torna-a definitiva num Lugar vago do serviço de destino, na mesma função e categoria")
    public ResponseEntity<ConsolidacaoMobilidadeResponseDTO> consolidar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody ConsolidacaoMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<ConsolidacaoMobilidadeResponseDTO> response =
                commandBus.send(new ConsolidarMobilidadeCommand(funcionarioId, licencaId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/cancel")
    @Operation(summary = "Cancelar licença/mobilidade (PENDING, ou APPROVED que ainda não começou → CANCELLED)")
    public ResponseEntity<SuccessResponseDTO> cancel(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new CancelarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/prorrogar")
    @Operation(summary = "Prorrogar licença/mobilidade em vigor (limites parametrizados no subtipo)")
    public ResponseEntity<ProrrogacaoMobilidadeResponseDTO> prorrogar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody ProrrogacaoMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<ProrrogacaoMobilidadeResponseDTO> response =
                commandBus.send(new ProrrogarLicencaMobilidadeCommand(licencaId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Legacy aliases (mantidos para compatibilidade) ───────────────────────

    @PatchMapping("{licencaId}/ativar")
    @Operation(summary = "Activar licença/mobilidade (alias de /approve, mantido por compatibilidade)")
    public ResponseEntity<LicencaEfeitoResponseDTO> ativar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<LicencaEfeitoResponseDTO> response = commandBus.send(new AtivarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{licencaId}/desativar")
    @Operation(summary = "Desactivar licença/mobilidade (alias de /cancel, mantido por compatibilidade)")
    public ResponseEntity<SuccessResponseDTO> desativar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new DesativarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("{licencaId}/documentos")
    @Operation(summary = "Associar documento a uma licença/mobilidade")
    public ResponseEntity<DocumentoUploadResponseDTO> registarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody UploadDocumentoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoUploadResponseDTO> response = commandBus.send(
                new RegistarDocumentoLicencaCommand(
                        funcionarioId, licencaId,
                        request.getDocumentTypeId(), request.getFileKey(),
                        request.getOriginalFilename(), request.getContentType(),
                        request.getFileSize(), request.getDescription()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{licencaId}/documentos")
    @Operation(summary = "Listar documentos de uma licença/mobilidade")
    public ResponseEntity<WrapperListaDocumentoDTO> listarDocumentos(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @RequestParam(required = false) java.util.UUID documentTypeId,
            @RequestParam(required = false) Boolean active) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(
                new GetDocumentosSubRecursoQuery("LICENCA_MOBILIDADE", java.util.UUID.fromString(licencaId), documentTypeId, active));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{licencaId}/documentos/{docId}")
    @Operation(summary = "Obter documento de uma licença por ID")
    public ResponseEntity<DocumentoResponseDTO> getDocumentoById(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoResponseDTO> response = queryBus.handle(new GetDocumentoByIdQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("{licencaId}/documentos/{docId}/download")
    @Operation(summary = "Obter URL de download de um documento da licença")
    public ResponseEntity<DocumentoDownloadResponseDTO> downloadDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<DocumentoDownloadResponseDTO> response = queryBus.handle(new GetDocumentoDownloadUrlQuery(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @DeleteMapping("{licencaId}/documentos/{docId}")
    @Operation(summary = "Desactivar documento de uma licença/mobilidade")
    public ResponseEntity<SuccessResponseDTO> desativarDocumento(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @PathVariable String docId) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new DesativarDocumentoCommand(funcionarioId, docId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
