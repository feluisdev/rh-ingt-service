/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.recrutamento.interfaces.rest;

import cv.igrp.RH_Service.recrutamento.application.commands.AccaoCandidaturaCommand;
import cv.igrp.RH_Service.recrutamento.application.commands.AccaoConcursoCommand;
import cv.igrp.RH_Service.recrutamento.application.commands.GuardarConcursoCommand;
import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaRequestDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoRequestDTO;
import cv.igrp.RH_Service.recrutamento.application.queries.GetConcursoQuery;
import cv.igrp.RH_Service.recrutamento.application.queries.GetConcursosQuery;
import cv.igrp.RH_Service.recrutamento.application.queries.GetMinhasCandidaturasQuery;
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
@RestController("recrutConcursoController")
@RequestMapping(path = "api/v1/rh")
@Tag(name = "Concurso", description = "Recrutamento e selecção por concurso (Lei n.º 20/X/2023, arts. 123.º a 129.º): aviso, júri, métodos, candidaturas registadas pelo RH, audiência prévia, lista de classificação, homologação, reserva e provimento")
public class ConcursoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcursoController.class);
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public ConcursoController(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @PostMapping("concursos")
    @Operation(summary = "Criar um concurso (em rascunho)")
    @ApiResponse(responseCode = "201", description = "Criado",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> criarConcurso(@RequestBody ConcursoRequestDTO request) {
        return enviar(new GuardarConcursoCommand(null, request));
    }

    @PutMapping("concursos/{concursoId}")
    @Operation(summary = "Alterar o concurso (só em rascunho; listas omitidas ficam como estão)")
    @ApiResponse(responseCode = "200", description = "Alterado",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> actualizarConcurso(@PathVariable String concursoId, @RequestBody ConcursoRequestDTO request) {
        return enviar(new GuardarConcursoCommand(concursoId, request));
    }

    @GetMapping("concursos")
    @Operation(summary = "Os concursos, do mais recente para o mais antigo (filtro opcional por estado)")
    @ApiResponse(responseCode = "200", description = "Concursos",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<List<ConcursoDTO>> getConcursos(@RequestParam(value = "estado", required = false) String estado) {
        return perguntar(new GetConcursosQuery(estado));
    }

    @GetMapping("concursos/{concursoId}")
    @Operation(summary = "O concurso com as candidaturas")
    @ApiResponse(responseCode = "200", description = "Concurso",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> getConcurso(@PathVariable String concursoId) {
        return perguntar(new GetConcursoQuery(concursoId));
    }

    @PatchMapping("concursos/{concursoId}/abrir")
    @Operation(summary = "Abrir o concurso (publicar o aviso): exige categoria, lugares, júri com presidente e dois vogais, métodos obrigatórios e prazo de candidatura")
    @ApiResponse(responseCode = "200", description = "Aberto",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> abrirConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "ABRIR", request));
    }

    @PatchMapping("concursos/{concursoId}/encerrar")
    @Operation(summary = "Encerrar o prazo das candidaturas")
    @ApiResponse(responseCode = "200", description = "Candidaturas encerradas",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> encerrarCandidaturasConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "ENCERRAR", request));
    }

    @PatchMapping("concursos/{concursoId}/avaliar")
    @Operation(summary = "Passar à avaliação (todas as candidaturas admitidas ou excluídas, audiências fechadas)")
    @ApiResponse(responseCode = "200", description = "Em avaliação",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> avaliarConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "AVALIAR", request));
    }

    @PatchMapping("concursos/{concursoId}/lista-provisoria")
    @Operation(summary = "Classificar e publicar a lista de classificação provisória (todas as notas lançadas)")
    @ApiResponse(responseCode = "200", description = "Lista provisória",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> listaProvisoriaConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "LISTA_PROVISORIA", request));
    }

    @PatchMapping("concursos/{concursoId}/homologar")
    @Operation(summary = "Homologar a lista final (despacho e data); a reserva de recrutamento fica válida por 18 meses")
    @ApiResponse(responseCode = "200", description = "Homologado",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> homologarConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "HOMOLOGAR", request));
    }

    @PatchMapping("concursos/{concursoId}/concluir")
    @Operation(summary = "Concluir o concurso")
    @ApiResponse(responseCode = "200", description = "Concluído",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> concluirConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "CONCLUIR", request));
    }

    @PatchMapping("concursos/{concursoId}/anular")
    @Operation(summary = "Anular o concurso, com motivo")
    @ApiResponse(responseCode = "200", description = "Anulado",
            content = @Content(schema = @Schema(implementation = ConcursoDTO.class)))
    public ResponseEntity<ConcursoDTO> anularConcurso(@PathVariable String concursoId, @RequestBody(required = false) ConcursoRequestDTO request) {
        return enviar(new AccaoConcursoCommand(concursoId, "ANULAR", request));
    }

    @PostMapping("concursos/{concursoId}/candidaturas")
    @Operation(summary = "Registar uma candidatura (pelo RH), dentro do prazo")
    @ApiResponse(responseCode = "201", description = "Registada",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> registarCandidatura(@PathVariable String concursoId, @RequestBody CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, null, "CANDIDATAR", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/admitir")
    @Operation(summary = "Admitir a candidatura")
    @ApiResponse(responseCode = "200", description = "Admitida",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> admitirCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                             @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "ADMITIR", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/excluir")
    @Operation(summary = "Propor a exclusão, com motivo (abre a audiência prévia de 10 dias úteis)")
    @ApiResponse(responseCode = "200", description = "Em audiência",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> excluirCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                             @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "EXCLUIR", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/audiencia")
    @Operation(summary = "Decidir a audiência prévia: excluir de vez ou admitir")
    @ApiResponse(responseCode = "200", description = "Decidida",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> decidirAudienciaCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                                      @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "AUDIENCIA", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/nota")
    @Operation(summary = "Lançar a nota de um método de selecção (0 a 20)")
    @ApiResponse(responseCode = "200", description = "Nota lançada",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> registarNotaCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                                  @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "NOTA", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/prover")
    @Operation(summary = "Prover o candidato num lugar (pela ordem da lista, com a quota de deficiência primeiro)")
    @ApiResponse(responseCode = "200", description = "Provido",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> proverCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                            @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "PROVER", request));
    }

    @PatchMapping("concursos/{concursoId}/candidaturas/{candidaturaId}/desistir")
    @Operation(summary = "Registar a desistência do candidato")
    @ApiResponse(responseCode = "200", description = "Desistiu",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<CandidaturaDTO> desistirCandidatura(@PathVariable String concursoId, @PathVariable String candidaturaId,
                                                              @RequestBody(required = false) CandidaturaRequestDTO request) {
        return enviar(new AccaoCandidaturaCommand(concursoId, candidaturaId, "DESISTIR", request));
    }

    @GetMapping("me/candidaturas")
    @Operation(summary = "As minhas candidaturas a concursos")
    @ApiResponse(responseCode = "200", description = "Candidaturas",
            content = @Content(schema = @Schema(implementation = CandidaturaDTO.class)))
    public ResponseEntity<List<CandidaturaDTO>> getMinhasCandidaturas() {
        return perguntar(new GetMinhasCandidaturasQuery());
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
