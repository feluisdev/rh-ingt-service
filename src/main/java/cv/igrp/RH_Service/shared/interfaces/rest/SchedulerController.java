package cv.igrp.RH_Service.shared.interfaces.rest;

import cv.igrp.RH_Service.shared.application.dto.AlterarEstadoSchedulerRequestDTO;
import cv.igrp.RH_Service.shared.application.dto.AtualizarSchedulerRequestDTO;
import cv.igrp.RH_Service.shared.application.dto.DispararSchedulerResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.ExecutarSchedulerRequestDTO;
import cv.igrp.RH_Service.shared.application.dto.SchedulerConfigDTO;
import cv.igrp.RH_Service.shared.application.dto.SchedulerExecucaoResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.WrapperListaSchedulerExecucoesDTO;
import cv.igrp.RH_Service.shared.application.dto.WrapperListaSchedulersDTO;
import cv.igrp.RH_Service.shared.application.services.scheduler.SchedulerService;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Gestão transversal dos jobs agendados: configuração, disparo manual, re-execução e histórico.
 *
 * <p>Escrito à mão, como o {@code DocumentController}, e não gerado pelo IGRP Studio: não há
 * manifesto em {@code .igrpstudio/} que o reescreva. A lógica vive no {@link SchedulerService}.
 */
@RestController
@RequestMapping("api/v1/rh/schedulers")
@Tag(name = "Schedulers", description = "Tarefas agendadas: configuração, disparo manual e histórico de execuções")
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final SecurityContextHelper securityContextHelper;

    public SchedulerController(SchedulerService schedulerService, SecurityContextHelper securityContextHelper) {
        this.schedulerService = schedulerService;
        this.securityContextHelper = securityContextHelper;
    }

    @GetMapping
    @Operation(
            summary = "Lista schedulers",
            description = "Configuração actual de todos os jobs registados, com paginação. Cada item traz a frequência, "
                    + "a hora, o estado (`activo`), a última e a próxima execução, e os parâmetros que o job aceita "
                    + "no disparo manual.\n\n"
                    + "**Filtros:** `nome` (parte do nome ou da chave, sem distinguir maiúsculas) e `frequencia` "
                    + "(código exacto, ex.: `DIARIO`).",
            responses = @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaSchedulersDTO.class))))
    public ResponseEntity<WrapperListaSchedulersDTO> listaSchedulers(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "frequencia", required = false) String frequencia,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {

        var filtrados = schedulerService.listarConfiguracoes().stream()
                .filter(c -> nome == null || nome.isBlank()
                        || c.getNome().toLowerCase().contains(nome.toLowerCase())
                        || c.getChave().toLowerCase().contains(nome.toLowerCase()))
                .filter(c -> frequencia == null || frequencia.isBlank() || frequencia.equals(c.getFrequencia()))
                .toList();

        int pn = pageNumber != null ? Math.max(pageNumber, 0) : 0;
        int ps = pageSize != null ? Math.max(pageSize, 1) : 20;
        int totalElements = filtrados.size();
        int totalPages = (int) Math.ceil((double) totalElements / ps);
        int fromIndex = Math.min(pn * ps, totalElements);
        int toIndex = Math.min(fromIndex + ps, totalElements);

        var wrapper = new WrapperListaSchedulersDTO();
        wrapper.setContent(filtrados.subList(fromIndex, toIndex));
        wrapper.setPageNumber(pn);
        wrapper.setPageSize(ps);
        wrapper.setTotalElements((long) totalElements);
        wrapper.setTotalPages(totalPages);
        wrapper.setFirst(pn == 0);
        wrapper.setLast(pn >= totalPages - 1);
        return ResponseEntity.ok(wrapper);
    }

    @GetMapping("{chave:[A-Z0-9_]+}")
    @Operation(
            summary = "Obter scheduler",
            description = "Configuração actual de um job: frequência, hora, descrição legível, estado, fuso horário, "
                    + "última e próxima execução, e a lista `parametros` — o contrato a partir do qual a interface "
                    + "gera o formulário do disparo manual.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SchedulerConfigDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Job não encontrado")
            })
    public ResponseEntity<SchedulerConfigDTO> getScheduler(@PathVariable("chave") String chave) {
        return ResponseEntity.ok(schedulerService.getConfiguracao(chave));
    }

    @PutMapping("{chave:[A-Z0-9_]+}")
    @Operation(
            summary = "Atualizar scheduler",
            description = "Muda o agendamento de um job. `frequencia` é obrigatória e decide que campos são precisos:\n\n"
                    + "- **DIARIO** → hora, minuto\n"
                    + "- **SEMANAL** → diaDaSemana, hora, minuto\n"
                    + "- **QUINZENAL** → hora, minuto (dias 1 e 15)\n"
                    + "- **MENSAL** → diaDoMes, hora, minuto\n"
                    + "- **TRIMESTRAL** → diaDoMes, hora, minuto (Jan, Abr, Jul, Out)\n"
                    + "- **SEMESTRAL** → diaDoMes, hora, minuto (Jan, Jul)\n"
                    + "- **ANUAL** → diaDoMes, mes, hora, minuto\n\n"
                    + "Os campos que a frequência não usa são ignorados. `timezone` é opcional; se omitido, mantém-se "
                    + "o actual. A alteração produz efeito sem reiniciar a aplicação — nas outras réplicas, no "
                    + "varrimento seguinte (até 5 minutos).",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Frequência inválida, campo em falta ou fora do "
                            + "intervalo, ou fuso horário desconhecido"),
                    @ApiResponse(responseCode = "404", description = "Job não encontrado")
            })
    public ResponseEntity<SuccessResponseDTO> atualizarScheduler(
            @PathVariable("chave") String chave,
            @Valid @RequestBody AtualizarSchedulerRequestDTO dto) {
        schedulerService.atualizarScheduler(chave, dto);
        return ResponseEntity.ok(SuccessResponseDTO.de(chave));
    }

    @PatchMapping("{chave:[A-Z0-9_]+}/activo")
    @Operation(
            summary = "Activar ou desactivar scheduler",
            description = "Suspende ou retoma um job sem perder a configuração.\n\n"
                    + "Um job suspenso não é agendado e deixa de ter próxima execução prevista, pelo que o período em "
                    + "que esteve suspenso **não** gera execuções em falta: não foram omissões, foi uma decisão. Ao "
                    + "reactivar, a próxima execução conta a partir desse momento.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "`activo` em falta"),
                    @ApiResponse(responseCode = "404", description = "Job não encontrado")
            })
    public ResponseEntity<SuccessResponseDTO> alterarEstadoScheduler(
            @PathVariable("chave") String chave,
            @Valid @RequestBody AlterarEstadoSchedulerRequestDTO dto) {
        schedulerService.alterarEstado(chave, Boolean.TRUE.equals(dto.getActivo()));
        return ResponseEntity.ok(SuccessResponseDTO.de(chave));
    }

    @PostMapping("{chave:[A-Z0-9_]+}/executar")
    @Operation(
            summary = "Disparar job manualmente",
            description = "Corre um job já, em segundo plano, tal como a execução agendada. Responde **202** com o `id` "
                    + "do registo de execução; o desfecho (SUCESSO/FALHA_PARCIAL/FALHA/TIMEOUT) consulta-se depois em "
                    + "GET execucoes/{id}.\n\n"
                    + "O corpo é **opcional**. Sem corpo, cada parâmetro assume o valor por omissão do job — nos jobs "
                    + "diários, o dia de hoje. Para tratar outro dia, indique-o:\n\n"
                    + "```json\n{ \"parametros\": { \"data\": \"2026-09-20\" } }\n```\n\n"
                    + "Os parâmetros aceites por cada job vêm em `parametros` de GET /schedulers/{chave}; um parâmetro "
                    + "desconhecido ou com valor inválido dá 400 antes de a execução abrir.\n\n"
                    + "> Para repetir uma execução que **falhou** use POST execucoes/{id}/reexecutar: esse reutiliza o "
                    + "instante e os parâmetros originais, enquanto este assume os de hoje.",
            responses = {
                    @ApiResponse(responseCode = "202", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DispararSchedulerResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Parâmetro desconhecido, em falta ou inválido"),
                    @ApiResponse(responseCode = "404", description = "Job não encontrado"),
                    @ApiResponse(responseCode = "409", description = "Já existe uma execução em curso para este job")
            })
    public ResponseEntity<DispararSchedulerResponseDTO> executarScheduler(
            @PathVariable("chave") String chave,
            @RequestBody(required = false) ExecutarSchedulerRequestDTO dto) {
        Map<String, Object> parametros = dto != null && dto.getParametros() != null ? dto.getParametros() : Map.of();
        var response = schedulerService.dispararManual(chave, parametros, securityContextHelper.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("execucoes/{id}/reexecutar")
    @Operation(
            summary = "Repetir uma execução",
            description = "Repete uma execução do histórico **com o mesmo instante e os mesmos parâmetros**, sem que o "
                    + "utilizador os tenha de indicar.\n\n"
                    + "É esta a diferença para POST {chave}/executar: aquele assume os valores de hoje; este lê-os de uma "
                    + "execução concreta do passado. Repetir a 3 de Janeiro a execução de 31 de Dezembro que falhou "
                    + "trata 31 de Dezembro.\n\n"
                    + "Serve igualmente uma execução `FALHA`, `TIMEOUT`, `FALHA_PARCIAL` (a idempotência do job salta o "
                    + "que já ficou feito) ou `OMITIDA` — um disparo que nunca chegou a acontecer. A nova execução fica "
                    + "ligada à original por `execucaoPaiId`.",
            responses = {
                    @ApiResponse(responseCode = "202", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DispararSchedulerResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                    @ApiResponse(responseCode = "404", description = "Execução não encontrada, ou job já não registado"),
                    @ApiResponse(responseCode = "409", description = "Já existe uma execução em curso para este job")
            })
    public ResponseEntity<DispararSchedulerResponseDTO> reexecutarScheduler(@PathVariable("id") String id) {
        var response = schedulerService.reexecutar(id, securityContextHelper.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("{chave:[A-Z0-9_]+}/execucoes")
    @Operation(
            summary = "Histórico de execuções de um job",
            description = "Execuções registadas de um job, das mais recentes para as mais antigas, com paginação. "
                    + "Filtros opcionais: `estado` (A_CORRER, SUCESSO, FALHA_PARCIAL, FALHA, TIMEOUT, OMITIDA), "
                    + "`disparo` (AGENDADO, MANUAL) e `referencia` (ex.: `2026`).\n\n"
                    + "`OMITIDA` é um disparo agendado que **não chegou a acontecer** — a aplicação estava parada à "
                    + "hora prevista. É uma linha criada pelo sistema, para que a ausência de uma execução se veja na "
                    + "mesma lista onde estão as que correram.",
            responses = @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WrapperListaSchedulerExecucoesDTO.class))))
    public ResponseEntity<WrapperListaSchedulerExecucoesDTO> listaExecucoes(
            @PathVariable("chave") String chave,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "disparo", required = false) String disparo,
            @RequestParam(value = "referencia", required = false) String referencia,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        return ResponseEntity.ok(
                schedulerService.listarExecucoes(chave, estado, disparo, referencia, pageNumber, pageSize));
    }

    @GetMapping("execucoes/{id}")
    @Operation(
            summary = "Obter execução",
            description = "Detalhe de uma execução: estado, tempos, duração, contadores "
                    + "(processados/criados/repetidos/saltados/falhas), parâmetros usados, número da tentativa, "
                    + "detalhes reportados pelo job (incluindo os itens que falharam) e, em caso de falha, o erro.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SchedulerExecucaoResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Identificador inválido"),
                    @ApiResponse(responseCode = "404", description = "Execução não encontrada")
            })
    public ResponseEntity<SchedulerExecucaoResponseDTO> getExecucao(@PathVariable("id") String id) {
        return ResponseEntity.ok(schedulerService.getExecucao(id));
    }
}
