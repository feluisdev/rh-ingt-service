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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;

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
    public ResponseEntity<MeProfileResponseDTO> getMyProfile() {
        LOGGER.debug("Operation started");
        ResponseEntity<MeProfileResponseDTO> response = queryBus.handle(new GetMeProfileQuery());
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
    public ResponseEntity<SuccessResponseDTO> createMyLeaveRequest(
            @Valid @RequestBody SelfServiceCriarPedidoAusenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
                new SelfServiceCriarPedidoAusenciaCommand(null, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("leave-requests/{id}/cancel")
    @Operation(summary = "Cancelar pedido de ausência próprio (apenas PENDING)")
    public ResponseEntity<SuccessResponseDTO> cancelMyLeaveRequest(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
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

    @GetMapping("leaves-mobilities/{id}")
    @Operation(summary = "Obter detalhe de licença/mobilidade própria (valida ownership)")
    public ResponseEntity<LicencaMobilidadeResponseDTO> getMyLeaveMobilityById(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<LicencaMobilidadeResponseDTO> response = queryBus.handle(new GetMeLicencaMobilidadeByIdQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("leaves-mobilities")
    @Operation(summary = "Auto-submeter licença/mobilidade (apenas quando canSelfSubmit=true no subtipo)")
    public ResponseEntity<SuccessResponseDTO> createMyLeaveMobility(
            @Valid @RequestBody SelfServiceCriarLicencaMobilidadeRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(
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
    public ResponseEntity<FileUrlDTO> getMyPayrollSlipDownloadUrl(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<FileUrlDTO> response = queryBus.handle(new GetMePayrollSlipDownloadQuery(id));
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
    public ResponseEntity<FileUrlDTO> getMyDocumentDownloadUrl(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<FileUrlDTO> response = queryBus.handle(new GetMeDocumentDownloadUrlQuery(id));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Assiduidade: registo pelo próprio e validação da chefia ────────────────────────────

    @PostMapping("marcacoes")
    @Operation(summary = "Picagem em tempo real pelo proprio (so em teletrabalho ou regime misto); a hora e a do servidor")
    @ApiResponse(responseCode = "201", description = "Picagem registada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> picar(@RequestBody cv.igrp.RH_Service.colaboradores.application.dto.PicagemPropriaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.PicarPeloProprioCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("marcacoes/correcoes")
    @Operation(summary = "Pedido de correcao do proprio (picagem esquecida); fica pendente ate a chefia ou o RH decidirem")
    @ApiResponse(responseCode = "201", description = "Pedido registado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> pedirCorrecao(@RequestBody cv.igrp.RH_Service.colaboradores.application.dto.CorrecaoMarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.PedirCorrecaoMarcacaoCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("assiduidade")
    @Operation(summary = "A minha assiduidade num periodo: por dia e por semana")
    @ApiResponse(responseCode = "200", description = "Assiduidade do periodo",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO> minhaAssiduidade(
            @RequestParam(value = "de") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate de,
            @RequestParam(value = "ate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate ate) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetMinhaAssiduidadeQuery(de, ate));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("equipa/marcacoes-pendentes")
    @Operation(summary = "Pedidos de correcao por decidir da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Pendentes",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoPendenteDTO.class))))
    public ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoPendenteDTO>> pendentesDaEquipa() {
        LOGGER.debug("Operation started");
        ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.MarcacaoPendenteDTO>> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetPendentesEquipaQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/marcacoes/{id}/validar")
    @Operation(summary = "Validar um pedido de correcao da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Validado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> validarDaEquipa(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirMarcacaoCommand(true, null, id, true, null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/marcacoes/{id}/rejeitar")
    @Operation(summary = "Rejeitar um pedido de correcao da minha equipa directa, com motivo")
    @ApiResponse(responseCode = "200", description = "Rejeitado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> rejeitarDaEquipa(@PathVariable String id,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.DecisaoMarcacaoRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirMarcacaoCommand(true, null, id, false, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Trabalho suplementar: pedido do próprio e decisão da chefia ────────────────────────

    @PostMapping("trabalho-suplementar")
    @Operation(summary = "Pedir trabalho suplementar (proprio), para hoje ou para a frente; fica PEDIDO ate a chefia ou o RH decidirem")
    @ApiResponse(responseCode = "201", description = "Pedido registado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> pedirTrabalhoSuplementar(@RequestBody cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.PedirTrabalhoSuplementarCommand(request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("trabalho-suplementar")
    @Operation(summary = "O meu trabalho suplementar do mes: horas autorizadas e realizadas")
    @ApiResponse(responseCode = "200", description = "Trabalho suplementar do mes",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarMesDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarMesDTO> meuTrabalhoSuplementar(@RequestParam(value = "mes") String mes) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarMesDTO> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetTrabalhoSuplementarMesQuery(null, mes));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PostMapping("equipa/trabalho-suplementar")
    @Operation(summary = "Lancar trabalho suplementar para alguem da minha equipa directa; nasce autorizado")
    @ApiResponse(responseCode = "201", description = "Trabalho suplementar autorizado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> lancarTrabalhoSuplementarDaEquipa(@RequestBody cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.LancarTrabalhoSuplementarCommand(true, null, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("equipa/trabalho-suplementar-pendente")
    @Operation(summary = "Pedidos de trabalho suplementar por decidir da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Pendentes",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarDTO.class))))
    public ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarDTO>> trabalhoSuplementarPendenteDaEquipa() {
        LOGGER.debug("Operation started");
        ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarDTO>> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetTrabalhoSuplementarPendenteEquipaQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/trabalho-suplementar/{id}/autorizar")
    @Operation(summary = "Autorizar um pedido de trabalho suplementar da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Autorizado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> autorizarTrabalhoSuplementarDaEquipa(@PathVariable String id) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirTrabalhoSuplementarCommand(true, null, id, true, null));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/trabalho-suplementar/{id}/recusar")
    @Operation(summary = "Recusar um pedido de trabalho suplementar da minha equipa directa, com motivo")
    @ApiResponse(responseCode = "200", description = "Recusado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> recusarTrabalhoSuplementarDaEquipa(@PathVariable String id,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.DecisaoTrabalhoSuplementarRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirTrabalhoSuplementarCommand(true, null, id, false, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Pedidos de ausência: decisão da chefia directa ─────────────────────────────────────

    @GetMapping("equipa/pedidos-ausencia-pendentes")
    @Operation(summary = "Pedidos de ausencia por decidir da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Pendentes",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaPendenteDTO.class))))
    public ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaPendenteDTO>> pedidosAusenciaPendentesDaEquipa() {
        LOGGER.debug("Operation started");
        ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaPendenteDTO>> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetPedidosAusenciaPendentesEquipaQuery());
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/pedidos-ausencia/{id}/aprovar")
    @Operation(summary = "Aprovar um pedido de ausencia da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Aprovado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> aprovarPedidoAusenciaDaEquipa(@PathVariable String id,
            @RequestBody(required = false) cv.igrp.RH_Service.colaboradores.application.dto.DecisaoPedidoAusenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirPedidoAusenciaEquipaCommand(id, true, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PatchMapping("equipa/pedidos-ausencia/{id}/rejeitar")
    @Operation(summary = "Rejeitar um pedido de ausencia da minha equipa directa, com motivo")
    @ApiResponse(responseCode = "200", description = "Rejeitado",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> rejeitarPedidoAusenciaDaEquipa(@PathVariable String id,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.DecisaoPedidoAusenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.DecidirPedidoAusenciaEquipaCommand(id, false, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    // ── Férias: preferência pelo próprio e a equipa para a chefia ─────────────────────────

    @GetMapping("ferias/{ano}")
    @Operation(summary = "As minhas ferias do ano: preferencia, marcacao e alteracoes")
    @ApiResponse(responseCode = "200", description = "Ferias do ano",
            content = @Content(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO.class)))
    public ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO> minhasFerias(@PathVariable Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetMinhasFeriasQuery(ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @PutMapping("ferias/{ano}/preferencia")
    @Operation(summary = "Indicar a minha preferencia de ferias (art. 5.o n.o 4); fora do prazo e aceite com alerta")
    @ApiResponse(responseCode = "200", description = "Preferencia registada",
            content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class)))
    public ResponseEntity<SuccessResponseDTO> indicarMinhaPreferencia(@PathVariable Integer ano,
            @RequestBody cv.igrp.RH_Service.colaboradores.application.dto.FeriasPreferenciaRequestDTO request) {
        LOGGER.debug("Operation started");
        ResponseEntity<SuccessResponseDTO> response = commandBus.send(new cv.igrp.RH_Service.colaboradores.application.commands.IndicarMinhaPreferenciaFeriasCommand(ano, request));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    @GetMapping("equipa/ferias/{ano}")
    @Operation(summary = "Preferencias e marcacoes de ferias da minha equipa directa")
    @ApiResponse(responseCode = "200", description = "Ferias da equipa",
            content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = cv.igrp.RH_Service.colaboradores.application.dto.FeriasEquipaLinhaDTO.class))))
    public ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.FeriasEquipaLinhaDTO>> feriasDaEquipa(@PathVariable Integer ano) {
        LOGGER.debug("Operation started");
        ResponseEntity<java.util.List<cv.igrp.RH_Service.colaboradores.application.dto.FeriasEquipaLinhaDTO>> response = queryBus.handle(new cv.igrp.RH_Service.colaboradores.application.queries.GetFeriasEquipaQuery(ano));
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
