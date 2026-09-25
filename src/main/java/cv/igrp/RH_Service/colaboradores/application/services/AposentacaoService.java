package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.application.commands.MudarEstadoColaboradorCommand;
import cv.igrp.RH_Service.colaboradores.application.commands.MudarEstadoColaboradorCommandHandler;
import cv.igrp.RH_Service.colaboradores.application.dto.MudarEstadoColaboradorRequestDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProrrogacaoPermanencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.AposentacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade;
import cv.igrp.RH_Service.colaboradores.domain.service.RegrasAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoAposentacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProrrogacaoPermanenciaId;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <b>Aposentação e limite de idade</b> (Lei n.º 20/X/2023, arts. 48.º, 93.º al. b), 96.º n.º 2 c),
 * 120.º n.º 1 d), 173.º–179.º; BR-APO-01..12).
 *
 * <ul>
 *   <li><b>Situação</b> de cada colaborador: quando faz 65 e 70 anos, até quando está prorrogado, quando
 *       reúne os 34 anos da antecipada e as condições da pré-aposentação — as datas projectam-se a
 *       partir da antiguidade de hoje.</li>
 *   <li><b>Relatório</b> por serviço: quem atinge alguma dessas datas até uma data.</li>
 *   <li><b>Processo</b>: pedido → despacho → desligação do serviço (inactividade no quadro aguardando
 *       aposentação) ou início da pré-aposentação → aposentação, que cessa o vínculo pelo
 *       {@link CessacaoService}. A antecipada extingue o Lugar (art. 178.º).</li>
 *   <li><b>Prorrogação</b> para além dos 65, até aos 70 (art. 48.º n.os 2 e 3).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AposentacaoService {

    static final String RECURSO = "PROCESSO_APOSENTACAO";

    /** O que se sabe da aposentação de uma pessoa, hoje. As datas de serviço são previsões. */
    public record Situacao(Funcionario funcionario, LocalDate hoje, int idade, LocalDate faz65, LocalDate faz70,
                           LocalDate prorrogadoAte, LocalDate limiteEfectivo,
                           CalculadoraAntiguidade.Antiguidade tempoServico, LocalDate completa34Anos,
                           LocalDate preAposentacaoPossivel, boolean podeAntecipada, boolean podePreAposentacao,
                           Optional<ProcessoAposentacao> processoEmCurso, List<ProcessoAposentacao> processos,
                           List<ProrrogacaoPermanencia> prorrogacoes, List<String> alertas) {}

    private final FuncionarioRepository funcionarioRepository;
    private final AposentacaoRepository aposentacaoRepository;
    private final AntiguidadeService antiguidadeService;
    private final QuemEstaNoServico quemEstaNoServico;
    private final CessacaoService cessacaoService;
    private final MudarEstadoColaboradorCommandHandler mudarEstado;
    private final WorkerStateRepository workerStateRepository;
    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final DiarioFactos diarioFactos;
    private final Notificador notificador;

    // ------------------------------------------------------------------ leitura

    @Transactional(readOnly = true)
    public Situacao situacao(FuncionarioId funcionarioId) {
        return situacao(funcionario(funcionarioId), hoje());
    }

    Situacao situacao(Funcionario f, LocalDate hoje) {
        LocalDate nasc = f.getDataNascimento();
        List<String> alertas = new ArrayList<>();
        if (nasc == null) alertas.add("Sem data de nascimento: não se calcula o limite de idade.");
        LocalDate faz65 = RegrasAposentacao.faz(nasc, RegrasAposentacao.IDADE_LIMITE);
        LocalDate faz70 = RegrasAposentacao.faz(nasc, RegrasAposentacao.IDADE_MAXIMA);
        List<ProrrogacaoPermanencia> prorrogacoes = aposentacaoRepository.findProrrogacoes(f.getId());
        LocalDate prorrogadoAte = prorrogacoes.stream().filter(ProrrogacaoPermanencia::isAutorizada)
                .map(ProrrogacaoPermanencia::getValidaAte).max(Comparator.naturalOrder()).orElse(null);
        LocalDate limite = prorrogadoAte != null ? prorrogadoAte : faz65;

        CalculadoraAntiguidade.Antiguidade tempo = null;
        LocalDate completa34 = null, pre = null;
        if (f.getDataAdmissao() != null) {
            tempo = antiguidadeService.calcular(f.getId(), hoje);
            completa34 = RegrasAposentacao.completaAnosDeServico(RegrasAposentacao.ANOS_SERVICO_ANTECIPADA, tempo.diasContados(), hoje);
            pre = RegrasAposentacao.preAposentacaoPossivel(nasc, tempo.diasContados(), hoje);
        } else {
            alertas.add("Sem data de admissão: não se calcula o tempo de serviço.");
        }
        if (limite != null && !hoje.isBefore(limite) && Boolean.TRUE.equals(f.getIsActive()))
            alertas.add(prorrogadoAte != null
                    ? "A prorrogação de permanência terminou em " + Datas.pt(prorrogadoAte) + "."
                    : "Atingiu o limite de idade (65 anos) em " + Datas.pt(faz65) + " sem prorrogação autorizada.");

        List<ProcessoAposentacao> processos = aposentacaoRepository.findProcessos(f.getId());
        Optional<ProcessoAposentacao> emCurso = processos.stream().filter(p -> p.getEstado().emCurso()).findFirst();
        return new Situacao(f, hoje, RegrasAposentacao.idade(nasc, hoje), faz65, faz70, prorrogadoAte, limite, tempo,
                completa34, pre, completa34 != null && !completa34.isAfter(hoje), pre != null && !pre.isAfter(hoje),
                emCurso, processos, prorrogacoes, alertas);
    }

    /**
     * O dia em que o vínculo cessa por idade: o fim da prorrogação autorizada ou os 65 anos (sem data de
     * nascimento, nulo). Leve: não calcula o tempo de serviço — é o que o alerta diário precisa.
     */
    @Transactional(readOnly = true)
    public LocalDate limiteEfectivo(Funcionario f) {
        LocalDate prorrogado = aposentacaoRepository.findProrrogacoes(f.getId()).stream()
                .filter(ProrrogacaoPermanencia::isAutorizada).map(ProrrogacaoPermanencia::getValidaAte)
                .max(Comparator.naturalOrder()).orElse(null);
        return prorrogado != null ? prorrogado : RegrasAposentacao.faz(f.getDataNascimento(), RegrasAposentacao.IDADE_LIMITE);
    }

    /**
     * Quem, no serviço (e subunidades), atinge o limite de idade, os 34 anos de serviço ou as condições da
     * pré-aposentação até {@code ate} — ou já tem um processo em curso. Ordenado pela data mais próxima.
     */
    @Transactional(readOnly = true)
    public List<Situacao> relatorio(UUID unidadeId, boolean subunidades, LocalDate ate) {
        LocalDate hoje = hoje();
        LocalDate limite = ate != null ? ate : hoje.plusMonths(12);
        var unidades = quemEstaNoServico.unidades(unidadeId, subunidades);
        var pessoas = quemEstaNoServico.colocacoes(unidades, hoje, hoje).keySet();
        List<Situacao> linhas = new ArrayList<>();
        for (Funcionario f : funcionarioRepository.findAllByIds(pessoas)) {
            if (!Boolean.TRUE.equals(f.getIsActive())) continue;
            Situacao s = situacao(f, hoje);
            LocalDate proxima = proxima(s);
            if (s.processoEmCurso().isPresent() || (proxima != null && !proxima.isAfter(limite))) linhas.add(s);
        }
        linhas.sort(Comparator.comparing(AposentacaoService::proxima, Comparator.nullsLast(Comparator.naturalOrder())));
        return linhas;
    }

    /** A primeira das datas que interessam (limite, 34 anos, pré-aposentação). */
    static LocalDate proxima(Situacao s) {
        return java.util.stream.Stream.of(s.limiteEfectivo(), s.completa34Anos(), s.preAposentacaoPossivel())
                .filter(java.util.Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    // ------------------------------------------------------------------ processo

    @Transactional
    public ProcessoAposentacao abrir(FuncionarioId funcionarioId, ModalidadeAposentacao modalidade,
                                     ProcessoAposentacao.Iniciativa iniciativa, LocalDate dataPrevista,
                                     String fundamentacao, boolean acordoFuncionario) {
        Funcionario f = funcionario(funcionarioId);
        if (!Boolean.TRUE.equals(f.getIsActive()))
            throw IgrpResponseStatusException.conflict("Este colaborador já não está ao serviço.");
        LocalDate hoje = hoje();
        Situacao s = situacao(f, hoje);
        if (s.processoEmCurso().isPresent())
            throw IgrpResponseStatusException.conflict("Já há um processo de aposentação em curso para este colaborador.");
        if (modalidade != null && iniciativa == ProcessoAposentacao.Iniciativa.FUNCIONARIO && !modalidade.podeSerPedidaPeloProprio())
            throw invalido("O próprio pode pedir a aposentação antecipada ou a pré-aposentação. As outras modalidades são abertas pelo RH.");
        LocalDate referencia = dataPrevista != null ? dataPrevista : hoje;
        if (modalidade == ModalidadeAposentacao.ANTECIPADA_PEDIDO && s.completa34Anos() != null
                && s.completa34Anos().isAfter(referencia))
            throw invalido("A aposentação antecipada a pedido exige 34 anos de serviço. Pelas contas de hoje, este colaborador "
                    + "completa-os em " + Datas.pt(s.completa34Anos()) + ".");
        if (modalidade == ModalidadeAposentacao.PRE_APOSENTACAO && s.preAposentacaoPossivel() != null
                && s.preAposentacaoPossivel().isAfter(referencia))
            throw invalido("A pré-aposentação exige 58 anos de idade e 30 de serviço. Pelas contas de hoje, este colaborador "
                    + "reúne as duas condições em " + Datas.pt(s.preAposentacaoPossivel()) + ".");
        if (modalidade == ModalidadeAposentacao.LIMITE_IDADE && s.faz65() != null && s.faz65().isAfter(referencia))
            throw invalido("Este colaborador só atinge o limite de idade em " + Datas.pt(s.faz65())
                    + ". Antes disso, a aposentação é antecipada.");

        var p = aposentacaoRepository.save(ProcessoAposentacao.abrir(funcionarioId, modalidade, iniciativa, hoje,
                dataPrevista, fundamentacao, acordoFuncionario));
        if (p.getIniciativa() == ProcessoAposentacao.Iniciativa.FUNCIONARIO)
            notificador.paraRh().tipo(TipoNotificacao.PROCESSO_APOSENTACAO)
                    .titulo(f.getNomeCompleto() + " pediu a " + nome(modalidade))
                    .texto("O pedido aguarda despacho.").recurso(RECURSO, p.getId().getStringValor()).enviar();
        return p;
    }

    @Transactional
    public ProcessoAposentacao deferir(FuncionarioId funcionarioId, ProcessoAposentacaoId id, String despachoNumero,
                                       LocalDate despachoData, LocalDate dataPrevista) {
        var p = processo(funcionarioId, id);
        p.deferir(despachoNumero, despachoData, dataPrevista);
        avisar(p, "Foi deferida a sua " + nome(p.getModalidade())
                + (p.getDataPrevista() != null ? ", com efeitos previstos a " + Datas.pt(p.getDataPrevista()) : "") + ".");
        return aposentacaoRepository.save(p);
    }

    @Transactional
    public ProcessoAposentacao indeferir(FuncionarioId funcionarioId, ProcessoAposentacaoId id, String motivo) {
        var p = processo(funcionarioId, id);
        p.indeferir(motivo);
        avisar(p, "Foi indeferida a sua " + nome(p.getModalidade()) + ".");
        return aposentacaoRepository.save(p);
    }

    /**
     * Desliga do serviço aguardando a aposentação (art. 120.º n.º 1 d)) — o estado passa ao indicado, ou
     * ao primeiro de inactividade no quadro do catálogo — ou inicia a pré-aposentação (art. 179.º), com a
     * prestação; aí o estado só muda se o RH indicar um.
     */
    @Transactional
    public ProcessoAposentacao desligar(FuncionarioId funcionarioId, ProcessoAposentacaoId id, LocalDate data,
                                        BigDecimal percentagemPrestacao, String workerStateId) {
        var p = processo(funcionarioId, id);
        p.desligar(data, percentagemPrestacao);
        boolean pre = p.getModalidade() == ModalidadeAposentacao.PRE_APOSENTACAO;
        Optional<WorkerState> estado = workerStateId != null && !workerStateId.isBlank()
                ? Optional.of(estado(workerStateId))
                : pre ? Optional.empty() : workerStateRepository.findBySituacao(SituacaoFuncional.INACTIVIDADE_NO_QUADRO);
        estado.ifPresent(e -> mudarEstadoPara(funcionarioId, e, data,
                pre ? "PRE_APOSENTACAO" : "AGUARDA_APOSENTACAO",
                pre ? "Início da pré-aposentação" : "Desligado do serviço aguardando aposentação"));
        if (pre) {
            var dados = new LinkedHashMap<String, Object>();
            dados.put("percentagemPrestacao", percentagemPrestacao.stripTrailingZeros().toPlainString());
            dados.put("processoId", p.getId().getStringValor());
            diarioFactos.registar(funcionarioId, TipoFactoRh.PRE_APOSENTACAO, data, RECURSO, p.getId().getStringValor(),
                    "Início da pré-aposentação, com prestação de " + percentagemPrestacao.stripTrailingZeros().toPlainString()
                            + "% da remuneração base", dados);
        }
        return aposentacaoRepository.save(p);
    }

    /**
     * A aposentação: cessa o vínculo pelo {@link CessacaoService}, com o estado indicado ou o primeiro de
     * aposentação do catálogo. Na antecipada, o Lugar deixado extingue-se (art. 178.º).
     */
    @Transactional
    public ProcessoAposentacao concluir(FuncionarioId funcionarioId, ProcessoAposentacaoId id, LocalDate data,
                                        String workerStateId, String observacao) {
        var p = processo(funcionarioId, id);
        p.concluir(data);
        WorkerState estado = workerStateId != null && !workerStateId.isBlank() ? estado(workerStateId)
                : workerStateRepository.findBySituacao(SituacaoFuncional.APOSENTACAO)
                .orElseThrow(() -> invalido("Não há no catálogo um estado de aposentação. Configure-o nos estados do colaborador."));
        if (!estado.isEndsEmployment())
            throw invalido("O estado escolhido não termina a relação de emprego. Escolha um estado de aposentação.");
        var cessacao = cessacaoService.cessar(funcionarioId, estado, data, "APOSENTACAO", observacao);
        if (p.getModalidade().extingueLugar() && cessacao.afectacaoEncerradaId() != null)
            extinguirLugar(cessacao.afectacaoEncerradaId());
        var dados = new LinkedHashMap<String, Object>();
        dados.put("modalidade", p.getModalidade().name());
        dados.put("processoId", p.getId().getStringValor());
        diarioFactos.registar(funcionarioId, TipoFactoRh.APOSENTACAO, data, RECURSO, p.getId().getStringValor(),
                "Aposentação: " + nome(p.getModalidade()), dados);
        avisar(p, "A sua aposentação produz efeitos a " + Datas.pt(data) + ".");
        return aposentacaoRepository.save(p);
    }

    @Transactional
    public ProcessoAposentacao cancelar(FuncionarioId funcionarioId, ProcessoAposentacaoId id, String motivo) {
        var p = processo(funcionarioId, id);
        p.cancelar(motivo);
        return aposentacaoRepository.save(p);
    }

    // ------------------------------------------------------------------ prorrogação

    @Transactional
    public ProrrogacaoPermanencia pedirProrrogacao(FuncionarioId funcionarioId, boolean manifestacaoVontade,
                                                   String propostaFundamentada, LocalDate validaAte) {
        Funcionario f = funcionario(funcionarioId);
        if (f.getDataNascimento() == null)
            throw invalido("Registe a data de nascimento do colaborador antes de pedir a prorrogação.");
        if (aposentacaoRepository.findProrrogacoes(funcionarioId).stream().anyMatch(x -> x.getEstado() == ProrrogacaoPermanencia.Estado.PEDIDA))
            throw IgrpResponseStatusException.conflict("Já há um pedido de prorrogação por decidir para este colaborador.");
        return aposentacaoRepository.save(ProrrogacaoPermanencia.pedir(funcionarioId, f.getDataNascimento(), hoje(),
                manifestacaoVontade, propostaFundamentada, validaAte));
    }

    @Transactional
    public ProrrogacaoPermanencia autorizarProrrogacao(FuncionarioId funcionarioId, ProrrogacaoPermanenciaId id,
                                                       String despachoNumero, LocalDate despachoData) {
        var p = prorrogacao(funcionarioId, id);
        p.autorizar(despachoNumero, despachoData);
        notificador.para(funcionarioId).tipo(TipoNotificacao.PROCESSO_APOSENTACAO)
                .titulo("Foi autorizada a sua permanência ao serviço até " + Datas.pt(p.getValidaAte()))
                .recurso("PRORROGACAO_PERMANENCIA", p.getId().getStringValor()).enviar();
        return aposentacaoRepository.save(p);
    }

    @Transactional
    public ProrrogacaoPermanencia indeferirProrrogacao(FuncionarioId funcionarioId, ProrrogacaoPermanenciaId id, String motivo) {
        var p = prorrogacao(funcionarioId, id);
        p.indeferir(motivo);
        notificador.para(funcionarioId).tipo(TipoNotificacao.PROCESSO_APOSENTACAO)
                .titulo("Não foi autorizada a sua permanência ao serviço para além dos 65 anos")
                .texto(p.getMotivoIndeferimento()).recurso("PRORROGACAO_PERMANENCIA", p.getId().getStringValor()).enviar();
        return aposentacaoRepository.save(p);
    }

    // ------------------------------------------------------------------ auxiliares

    private void mudarEstadoPara(FuncionarioId funcionarioId, WorkerState estado, LocalDate data, String motivo, String observacao) {
        Funcionario f = funcionario(funcionarioId);
        if (estado.getId().getValor().equals(f.getWorkerStateId())) return;
        var req = new MudarEstadoColaboradorRequestDTO();
        req.setWorkerStateId(estado.getId().getStringValor());
        req.setDataEfectividade(data);
        req.setMotivoCkey(motivo);
        req.setObservacao(observacao);
        mudarEstado.handle(new MudarEstadoColaboradorCommand(funcionarioId.getStringValor(), req));
    }

    private void extinguirLugar(UUID afectacaoId) {
        assignmentRepository.findById(AssignmentId.from(afectacaoId))
                .map(Assignment::getPositionId)
                .flatMap(pos -> positionRepository.findById(PositionId.from(pos)))
                .ifPresent(pos -> {
                    pos.extinguir();
                    positionRepository.save(pos);
                });
    }

    private void avisar(ProcessoAposentacao p, String titulo) {
        notificador.para(p.getFuncionarioId()).tipo(TipoNotificacao.PROCESSO_APOSENTACAO)
                .titulo(titulo).recurso(RECURSO, p.getId().getStringValor()).enviar();
    }

    static String nome(ModalidadeAposentacao m) {
        if (m == null) return "aposentação";
        return switch (m) {
            case LIMITE_IDADE -> "aposentação por limite de idade";
            case ANTECIPADA_PEDIDO -> "aposentação antecipada";
            case ANTECIPADA_INTERESSE_ADMINISTRACAO -> "aposentação antecipada no interesse da Administração";
            case INVALIDEZ -> "aposentação por invalidez";
            case PRE_APOSENTACAO -> "pré-aposentação";
            case COMPULSIVA -> "aposentação compulsiva";
        };
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    private ProcessoAposentacao processo(FuncionarioId funcionarioId, ProcessoAposentacaoId id) {
        return aposentacaoRepository.findProcesso(id).filter(p -> p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Processo de aposentação não encontrado."));
    }

    private ProrrogacaoPermanencia prorrogacao(FuncionarioId funcionarioId, ProrrogacaoPermanenciaId id) {
        return aposentacaoRepository.findProrrogacao(id).filter(p -> p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Prorrogação não encontrada."));
    }

    private WorkerState estado(String workerStateId) {
        try {
            return workerStateRepository.findById(WorkerStateId.from(UUID.fromString(workerStateId.trim())))
                    .orElseThrow(() -> invalido("O estado indicado não existe."));
        } catch (IllegalArgumentException e) {
            throw invalido("O estado indicado não existe.");
        }
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
