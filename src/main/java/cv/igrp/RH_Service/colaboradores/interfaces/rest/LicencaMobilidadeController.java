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
import java.util.UUID;

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
    public ResponseEntity<Map<String, ?>> create(
            @PathVariable String funcionarioId,
            @Valid @RequestBody LicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CreateLicencaMobilidadeCommand(funcionarioId, request));
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
    public ResponseEntity<Map<String, ?>> update(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody LicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new UpdateLicencaMobilidadeCommand(funcionarioId, licencaId, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Workflow ─────────────────────────────────────────────────────────────

    @PutMapping("{licencaId}/approve")
    @Operation(summary = "Aprovar licença/mobilidade (PENDING → ACTIVE; mobilidades criam nova colocação)")
    public ResponseEntity<Map<String, ?>> approve(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AprovarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/reject")
    @Operation(summary = "Rejeitar licença/mobilidade (PENDING → REJECTED)")
    public ResponseEntity<Map<String, ?>> reject(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId,
            @Valid @RequestBody RejeitarLicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new RejeitarLicencaMobilidadeCommand(licencaId, request.getRejectionReason()));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/close")
    @Operation(summary = "Encerrar licença/mobilidade (ACTIVE → CLOSED; mobilidades restauram colocação anterior)")
    public ResponseEntity<Map<String, ?>> close(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new EncerrarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("{licencaId}/cancel")
    @Operation(summary = "Cancelar licença/mobilidade (PENDING ou ACTIVE → CANCELLED)")
    public ResponseEntity<Map<String, ?>> cancel(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new CancelarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Legacy aliases (mantidos para compatibilidade) ───────────────────────

    @PatchMapping("{licencaId}/ativar")
    @Operation(summary = "Activar licença/mobilidade (alias de /approve, mantido por compatibilidade)")
    public ResponseEntity<Map<String, ?>> ativar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new AtivarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("{licencaId}/desativar")
    @Operation(summary = "Desactivar licença/mobilidade (alias de /cancel, mantido por compatibilidade)")
    public ResponseEntity<Map<String, ?>> desativar(
            @PathVariable String funcionarioId,
            @PathVariable String licencaId) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(new DesativarLicencaMobilidadeCommand(licencaId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
