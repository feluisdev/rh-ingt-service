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
@RestController("colabsMeController")
@RequestMapping(path = "api/v1/rh/me")
@Tag(name = "Meu Perfil", description = "Área reservada do colaborador — self-service com ROLE_FUNCIONARIO")
public class MeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public MeController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    // ── US1: Perfil ──────────────────────────────────────────────────────────

    @GetMapping("profile")
    @Operation(summary = "Obter perfil completo do colaborador autenticado")
    public ResponseEntity<MeProfileResponse> getMyProfile() {
        LOGGER.debug("Operation started");
        ResponseEntity<MeProfileResponse> response = queryBus.handle(new GetMeProfileQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── US2: Ausências ───────────────────────────────────────────────────────

    @GetMapping("leave-requests")
    @Operation(summary = "Listar pedidos de ausência do colaborador autenticado")
    public ResponseEntity<WrapperListaPedidoAusenciaDTO> getMyLeaveRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveTypeId,
            @RequestParam(required = false) Integer year) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaPedidoAusenciaDTO> response = queryBus.handle(
                new GetMeLeaveRequestsQuery(status, leaveTypeId, year));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("leave-balances")
    @Operation(summary = "Consultar saldos de ausência do colaborador autenticado")
    public ResponseEntity<WrapperListaSaldoAusenciaDTO> getMyLeaveBalances() {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaSaldoAusenciaDTO> response = queryBus.handle(new GetMeLeaveBalancesQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("leave-requests")
    @Operation(summary = "Submeter pedido de ausência (self-service)")
    public ResponseEntity<Map<String, ?>> createMyLeaveRequest(
            @Valid @RequestBody SelfServiceCriarPedidoAusenciaRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new SelfServiceCriarPedidoAusenciaCommand(null, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("leave-requests/{id}/cancel")
    @Operation(summary = "Cancelar pedido de ausência próprio (apenas PENDING)")
    public ResponseEntity<Map<String, ?>> cancelMyLeaveRequest(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new SelfServiceCancelarPedidoAusenciaCommand(null, id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── US3: Licenças e Mobilidades ──────────────────────────────────────────

    @GetMapping("leaves-mobilities")
    @Operation(summary = "Listar licenças e mobilidades do colaborador autenticado")
    public ResponseEntity<WrapperListaLicencaMobilidadeDTO> getMyLeavesMobilities() {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaLicencaMobilidadeDTO> response = queryBus.handle(new GetMeLeaveMobilitiesQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("leaves-mobilities")
    @Operation(summary = "Auto-submeter licença/mobilidade (apenas quando canSelfSubmit=true no subtipo)")
    public ResponseEntity<Map<String, ?>> createMyLeaveMobility(
            @Valid @RequestBody SelfServiceCriarLicencaMobilidadeRequest request) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = commandBus.send(
                new SelfServiceCriarLicencaMobilidadeCommand(null, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── US5: Recibos de Vencimento ───────────────────────────────────────────

    @GetMapping("payroll-slips")
    @Operation(summary = "Listar recibos de vencimento do colaborador autenticado")
    public ResponseEntity<WrapperListaReciboDTO> getMyPayrollSlips(
            @RequestParam(required = false) Integer periodYear,
            @RequestParam(required = false) Integer periodMonth) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaReciboDTO> response = queryBus.handle(
                new GetMePayrollSlipsQuery(periodYear, periodMonth));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("payroll-slips/{id}/download")
    @Operation(summary = "Obter URL de download do PDF do recibo próprio (valida ownership)")
    public ResponseEntity<Map<String, ?>> getMyPayrollSlipDownloadUrl(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = queryBus.handle(new GetMePayrollSlipDownloadQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── US4: Documentos ──────────────────────────────────────────────────────

    @GetMapping("documents")
    @Operation(summary = "Listar documentos do colaborador autenticado")
    public ResponseEntity<WrapperListaDocumentoDTO> getMyDocuments(
            @RequestParam(required = false) String documentTypeId) {
        LOGGER.debug("Operation started");
        ResponseEntity<WrapperListaDocumentoDTO> response = queryBus.handle(new GetMeDocumentsQuery(documentTypeId));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("documents/{id}/download")
    @Operation(summary = "Obter URL de download de documento próprio (valida ownership)")
    public ResponseEntity<Map<String, ?>> getMyDocumentDownloadUrl(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<Map<String, ?>> response = queryBus.handle(new GetMeDocumentDownloadUrlQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
