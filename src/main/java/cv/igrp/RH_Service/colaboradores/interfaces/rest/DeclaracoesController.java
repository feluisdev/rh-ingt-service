/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.interfaces.rest;

import cv.igrp.RH_Service.colaboradores.application.commands.AnularDocumentoEmitidoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.DecidirDeclaracaoCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.PedirDeclaracaoCommand;
import cv.igrp.RH_Service.colaboradores.application.dto.AnulacaoDocumentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoEmitidoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.VerificacaoDocumentoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.GetDeclaracoesQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetDocumentosEmitidosQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.GetLinkDocumentoEmitidoQuery;
import cv.igrp.RH_Service.colaboradores.application.queries.VerificarDocumentoQuery;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;
import cv.igrp.framework.core.domain.Command;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.Query;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.framework.stereotype.IgrpController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@IgrpController
@RestController("colabsDeclaracoesController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Declaracoes", description = "Declarações e documentos emitidos pelo RH: pedido (pelo próprio ou pelo RH), emissão numerada em PDF (guardado no MinIO), anulação e verificação pública pelo código")
public class DeclaracoesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeclaracoesController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public DeclaracoesController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("funcionarios/{funcionarioId}/declaracoes")
    @Operation(summary = "Emitir uma declaração (RH): regista o pedido e emite logo o PDF numerado")
    @ApiResponse(responseCode = "201", description = "Declaração emitida",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<PedidoDeclaracaoDTO> emitirDeclaracao(
            @PathVariable String funcionarioId, @RequestBody PedidoDeclaracaoRequestDTO request) {
        return enviar(new PedirDeclaracaoCommand(false, funcionarioId, request));
    }

    @GetMapping("funcionarios/{funcionarioId}/declaracoes")
    @Operation(summary = "Os pedidos de declaração do colaborador, com o documento emitido")
    @ApiResponse(responseCode = "200", description = "Pedidos",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<List<PedidoDeclaracaoDTO>> getDeclaracoes(@PathVariable String funcionarioId) {
        return perguntar(new GetDeclaracoesQuery(funcionarioId, false));
    }

    @GetMapping("declaracoes/por-emitir")
    @Operation(summary = "A caixa do RH: os pedidos de declaração por emitir, dos mais antigos para os mais recentes")
    @ApiResponse(responseCode = "200", description = "Pedidos por emitir",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<List<PedidoDeclaracaoDTO>> getDeclaracoesPorEmitir() {
        return perguntar(new GetDeclaracoesQuery(null, true));
    }

    @PatchMapping("funcionarios/{funcionarioId}/declaracoes/{pedidoId}/emitir")
    @Operation(summary = "Emitir um pedido de declaração")
    @ApiResponse(responseCode = "200", description = "Emitida",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<PedidoDeclaracaoDTO> emitirPedidoDeclaracao(@PathVariable String funcionarioId, @PathVariable String pedidoId) {
        return enviar(new DecidirDeclaracaoCommand(funcionarioId, pedidoId, true, null));
    }

    @PatchMapping("funcionarios/{funcionarioId}/declaracoes/{pedidoId}/recusar")
    @Operation(summary = "Recusar um pedido de declaração, com motivo")
    @ApiResponse(responseCode = "200", description = "Recusada",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<PedidoDeclaracaoDTO> recusarPedidoDeclaracao(@PathVariable String funcionarioId, @PathVariable String pedidoId,
                                                                       @RequestBody PedidoDeclaracaoRequestDTO request) {
        return enviar(new DecidirDeclaracaoCommand(funcionarioId, pedidoId, false, request));
    }

    @GetMapping("funcionarios/{funcionarioId}/documentos-emitidos")
    @Operation(summary = "Os documentos emitidos para o colaborador (declarações, cartão, extractos)")
    @ApiResponse(responseCode = "200", description = "Documentos",
            content = @Content(schema = @Schema(implementation = DocumentoEmitidoDTO.class)))
    public ResponseEntity<List<DocumentoEmitidoDTO>> getDocumentosEmitidos(@PathVariable String funcionarioId) {
        return perguntar(new GetDocumentosEmitidosQuery(funcionarioId));
    }

    @GetMapping("documentos-emitidos/{documentoId}/link")
    @Operation(summary = "O link (assinado, temporário) para descarregar o PDF do documento")
    @ApiResponse(responseCode = "200", description = "Link",
            content = @Content(schema = @Schema(implementation = FileUrlDTO.class)))
    public ResponseEntity<FileUrlDTO> getLinkDocumentoEmitido(@PathVariable String documentoId) {
        return perguntar(new GetLinkDocumentoEmitidoQuery(documentoId, false));
    }

    @PatchMapping("documentos-emitidos/{documentoId}/anular")
    @Operation(summary = "Anular um documento emitido, com motivo (a verificação passa a dizer que foi anulado)")
    @ApiResponse(responseCode = "200", description = "Anulado",
            content = @Content(schema = @Schema(implementation = DocumentoEmitidoDTO.class)))
    public ResponseEntity<DocumentoEmitidoDTO> anularDocumentoEmitido(@PathVariable String documentoId,
                                                                      @RequestBody AnulacaoDocumentoRequestDTO request) {
        return enviar(new AnularDocumentoEmitidoCommand(documentoId, request));
    }

    @GetMapping("verificacao/documentos/{codigo}")
    @Operation(summary = "Verificação pública de um documento pelo código impresso (não exige autenticação)")
    @ApiResponse(responseCode = "200", description = "Resultado da verificação",
            content = @Content(schema = @Schema(implementation = VerificacaoDocumentoDTO.class)))
    public ResponseEntity<VerificacaoDocumentoDTO> verificarDocumento(@PathVariable String codigo) {
        return perguntar(new VerificarDocumentoQuery(codigo));
    }

    @PostMapping("me/declaracoes")
    @Operation(summary = "Pedir uma declaração (o próprio); o RH emite-a")
    @ApiResponse(responseCode = "201", description = "Pedido registado",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<PedidoDeclaracaoDTO> pedirMinhaDeclaracao(@RequestBody PedidoDeclaracaoRequestDTO request) {
        return enviar(new PedirDeclaracaoCommand(true, null, request));
    }

    @GetMapping("me/declaracoes")
    @Operation(summary = "Os meus pedidos de declaração")
    @ApiResponse(responseCode = "200", description = "Pedidos",
            content = @Content(schema = @Schema(implementation = PedidoDeclaracaoDTO.class)))
    public ResponseEntity<List<PedidoDeclaracaoDTO>> getMinhasDeclaracoes() {
        return perguntar(new GetDeclaracoesQuery(null, false));
    }

    @GetMapping("me/documentos-emitidos")
    @Operation(summary = "Os meus documentos emitidos")
    @ApiResponse(responseCode = "200", description = "Documentos",
            content = @Content(schema = @Schema(implementation = DocumentoEmitidoDTO.class)))
    public ResponseEntity<List<DocumentoEmitidoDTO>> getMeusDocumentosEmitidos() {
        return perguntar(new GetDocumentosEmitidosQuery(null));
    }

    @GetMapping("me/documentos-emitidos/{documentoId}/link")
    @Operation(summary = "O link para descarregar um documento meu")
    @ApiResponse(responseCode = "200", description = "Link",
            content = @Content(schema = @Schema(implementation = FileUrlDTO.class)))
    public ResponseEntity<FileUrlDTO> getLinkMeuDocumentoEmitido(@PathVariable String documentoId) {
        return perguntar(new GetLinkDocumentoEmitidoQuery(documentoId, true));
    }

    private <T> ResponseEntity<T> enviar(Command command) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = commandBus.send(command);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }

    private <T> ResponseEntity<T> perguntar(Query query) {
        LOGGER.debug("Operation started");
        ResponseEntity<T> response = queryBus.handle(query);
        LOGGER.debug("Operation finished");
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
    }
}
