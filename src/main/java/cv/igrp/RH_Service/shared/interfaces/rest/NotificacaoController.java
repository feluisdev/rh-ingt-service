package cv.igrp.RH_Service.shared.interfaces.rest;

import cv.igrp.RH_Service.shared.application.dto.ContagemNotificacoesDTO;
import cv.igrp.RH_Service.shared.application.dto.NotificacaoDTO;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.WrapperListaNotificacoesDTO;
import cv.igrp.RH_Service.shared.application.services.notificacoes.NotificacaoService;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.Notificacao;
import cv.igrp.RH_Service.shared.domain.notificacoes.NotificacaoId;
import cv.igrp.RH_Service.shared.domain.notificacoes.PerfilDestino;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Notificações na aplicação: a caixa de cada pessoa ({@code /me/notificacoes}) e as caixas partilhadas
 * dos perfis ({@code /notificacoes?perfil=RH}). Quem escreve notificações é o código, pelo
 * {@code Notificador}; aqui só se lê e se marca lida.
 *
 * <p>Escrito à mão, como o {@code SchedulerController}: é transversal e não tem manifesto em
 * {@code .igrpstudio/} que o reescreva.
 */
@RestController
@RequestMapping("api/v1/rh")
@Tag(name = "Notificacoes", description = "Notificações na aplicação: a caixa de cada pessoa e a caixa partilhada do RH (o envio por correio electrónico ainda não existe)")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    public NotificacaoController(NotificacaoService notificacaoService, CurrentEmployeeResolver currentEmployeeResolver) {
        this.notificacaoService = notificacaoService;
        this.currentEmployeeResolver = currentEmployeeResolver;
    }

    @GetMapping("me/notificacoes")
    @Operation(summary = "As minhas notificações, das mais recentes para as mais antigas; naoLidas=true filtra as por ler")
    @ApiResponse(responseCode = "200", description = "Uma página de notificações, com o total por ler",
            content = @Content(schema = @Schema(implementation = WrapperListaNotificacoesDTO.class)))
    public ResponseEntity<WrapperListaNotificacoesDTO> minhas(
            @RequestParam(value = "naoLidas", required = false) Boolean naoLidas,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        var eu = currentEmployeeResolver.resolve();
        var pagina = notificacaoService.minhas(eu, Boolean.TRUE.equals(naoLidas), page, size);
        return ResponseEntity.ok(wrapper(pagina, notificacaoService.naoLidas(eu)));
    }

    @GetMapping("me/notificacoes/contagem")
    @Operation(summary = "Quantas notificações minhas estão por ler")
    @ApiResponse(responseCode = "200", description = "Contagem das por ler",
            content = @Content(schema = @Schema(implementation = ContagemNotificacoesDTO.class)))
    public ResponseEntity<ContagemNotificacoesDTO> minhasPorLer() {
        return ResponseEntity.ok(new ContagemNotificacoesDTO(notificacaoService.naoLidas(currentEmployeeResolver.resolve())));
    }

    @PatchMapping("me/notificacoes/{notificacaoId}/lida")
    @Operation(summary = "Marcar lida uma notificação minha")
    @ApiResponse(responseCode = "200", description = "Notificação lida",
            content = @Content(schema = @Schema(implementation = NotificacaoDTO.class)))
    public ResponseEntity<NotificacaoDTO> marcarMinhaLida(@PathVariable String notificacaoId) {
        return ResponseEntity.ok(dto(notificacaoService.marcarLida(currentEmployeeResolver.resolve(), id(notificacaoId))));
    }

    @PatchMapping("me/notificacoes/lidas")
    @Operation(summary = "Marcar lidas todas as minhas notificações por ler")
    @ApiResponse(responseCode = "200", description = "Quantas passaram a lidas",
            content = @Content(schema = @Schema(implementation = ContagemNotificacoesDTO.class)))
    public ResponseEntity<ContagemNotificacoesDTO> marcarTodasLidas() {
        return ResponseEntity.ok(new ContagemNotificacoesDTO(notificacaoService.marcarTodasLidas(currentEmployeeResolver.resolve())));
    }

    @GetMapping("notificacoes")
    @Operation(summary = "A caixa partilhada de um perfil (hoje só RH), das mais recentes para as mais antigas")
    @ApiResponse(responseCode = "200", description = "Uma página de notificações, com o total por ler",
            content = @Content(schema = @Schema(implementation = WrapperListaNotificacoesDTO.class)))
    public ResponseEntity<WrapperListaNotificacoesDTO> doPerfil(
            @RequestParam(value = "perfil", required = false, defaultValue = "RH") String perfil,
            @RequestParam(value = "naoLidas", required = false) Boolean naoLidas,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        PerfilDestino p = perfil(perfil);
        var pagina = notificacaoService.doPerfil(p, Boolean.TRUE.equals(naoLidas), page, size);
        return ResponseEntity.ok(wrapper(pagina, notificacaoService.naoLidas(p)));
    }

    @PatchMapping("notificacoes/{notificacaoId}/lida")
    @Operation(summary = "Marcar lida uma notificação da caixa partilhada (fica lida para todo o perfil)")
    @ApiResponse(responseCode = "200", description = "Notificação lida",
            content = @Content(schema = @Schema(implementation = NotificacaoDTO.class)))
    public ResponseEntity<NotificacaoDTO> marcarDoPerfilLida(
            @PathVariable String notificacaoId,
            @RequestParam(value = "perfil", required = false, defaultValue = "RH") String perfil) {
        return ResponseEntity.ok(dto(notificacaoService.marcarLida(perfil(perfil), id(notificacaoId))));
    }

    private static WrapperListaNotificacoesDTO wrapper(PageResult<Notificacao> p, long naoLidas) {
        var w = new WrapperListaNotificacoesDTO();
        w.setContent(p.getData().stream().map(NotificacaoController::dto).toList());
        w.setNaoLidas(naoLidas);
        w.setPageNumber(p.getPageNumber());
        w.setPageSize(p.getPageSize());
        w.setTotalElements(p.getTotalElements());
        w.setTotalPages(p.getTotalPages());
        w.setFirst(p.isFirst());
        w.setLast(p.isLast());
        return w;
    }

    static NotificacaoDTO dto(Notificacao n) {
        return new NotificacaoDTO(n.getId().getStringValor(), n.getTipo().name(), n.getTitulo(), n.getTexto(),
                n.getRecursoTipo(), n.getRecursoId(), n.getPerfil() != null ? n.getPerfil().name() : null,
                n.getCriadaEm(), n.getLidaEm(), n.isLida());
    }

    private static NotificacaoId id(String valor) {
        try {
            return NotificacaoId.from(valor.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw IgrpResponseStatusException.notFound("Notificação não encontrada.");
        }
    }

    private static PerfilDestino perfil(String valor) {
        try {
            return PerfilDestino.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Essa caixa de notificações não existe. Use a caixa do RH.");
        }
    }
}
