package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeProvimento;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.models.Provimento;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProvimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * <b>Entrada ao serviço</b> (Lei n.º 20/X/2023, arts. 52.º–81.º, 94.º n.º 3; BR-PRV-01..14): o provimento (forma de
 * vínculo, despacho, posse) e o período de prova que o acompanha — estágio probatório (1 ano, tutor, relatório) ou
 * período experimental (60/30 dias). O fim com sucesso converte o estágio em nomeação definitiva ou contrato por
 * tempo indeterminado; sem sucesso, a exoneração obrigatória (ou o regresso à carreira de origem) e, no período
 * experimental, a cessação do contrato — pelo caminho único da cessação.
 */
@Service
@RequiredArgsConstructor
public class ProvimentoService {

    static final String RECURSO = "PERIODO_PROVA";

    /** O resultado de um passo, com o que o RH ainda tem de fazer. */
    public record Resultado<T>(T valor, List<String> alertas) {}

    private final ProvimentoRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final CessacaoService cessacaoService;
    private final WorkerStateRepository workerStateRepository;
    private final DiarioFactos diarioFactos;
    private final Notificador notificador;
    private final ChecklistService checklists;

    @Transactional
    public Resultado<Provimento> registar(FuncionarioId funcionarioId, ModalidadeProvimento modalidade, String despachoNumero,
                                          LocalDate despachoData, LocalDate dataPosse, String concursoRef,
                                          boolean vemDeOutraCarreira, FuncionarioId tutorId, Integer mesesPrevistos,
                                          String observacoes) {
        Funcionario f = funcionario(funcionarioId);
        if (!Boolean.TRUE.equals(f.getIsActive()))
            throw invalido("Este colaborador já não está ao serviço.");
        var p = Provimento.registar(funcionarioId, modalidade, despachoNumero, despachoData, dataPosse, concursoRef,
                vemDeOutraCarreira, null, observacoes);
        List<String> alertas = new ArrayList<>();
        PeriodoProva periodo = null;
        if (modalidade.temEstagioProbatorio()) {
            if (tutorId != null) {
                var tutor = funcionario(tutorId);
                if (!Boolean.TRUE.equals(tutor.getIsActive())) throw invalido("O tutor indicado já não está ao serviço.");
            }
            periodo = PeriodoProva.estagio(p.getId(), funcionarioId, dataPosse, tutorId);
        } else if (modalidade.temPeriodoExperimental()) {
            LocalDate fim = modalidade == ModalidadeProvimento.CONTRATO_TERMO_CERTO
                    ? contratoRepository.findCurrentByFuncionarioId(funcionarioId).map(c -> c.getEndDate()).orElse(null) : null;
            if (modalidade == ModalidadeProvimento.CONTRATO_TERMO_CERTO && fim == null)
                throw invalido("Registe primeiro o contrato a termo certo, com a data do termo: o período experimental depende da duração.");
            periodo = PeriodoProva.experimental(p.getId(), funcionarioId, dataPosse, fim, mesesPrevistos);
        }
        if (periodo != null) {
            p.comPeriodoProva(periodo.getId());
            repository.save(p);
            repository.save(periodo);
            if (periodo.getTutorId() != null)
                notificador.para(periodo.getTutorId()).tipo(TipoNotificacao.PERIODO_PROVA)
                        .titulo("Foi designado tutor do estágio probatório de " + f.getNomeCompleto())
                        .texto("O estágio termina a " + Datas.pt(periodo.getFimPrevisto()) + "; remeta o relatório final antes dessa data.")
                        .recurso(RECURSO, periodo.getId().getStringValor()).enviar();
            alertas.add((periodo.getTipo() == PeriodoProva.Tipo.ESTAGIO_PROBATORIO ? "Estágio probatório" : "Período experimental")
                    + " até " + Datas.pt(periodo.getFimPrevisto()) + ".");
        } else {
            p = repository.save(p);
        }
        checklists.cumprir(funcionarioId, TipoChecklist.ENTRADA, ChecklistService.PROVIMENTO,
                "Provimento registado; posse a " + Datas.pt(dataPosse) + ".", null);
        return new Resultado<>(p, alertas);
    }

    /** O tutor remete o relatório final (art. 72.º n.º 4); o RH decide depois. */
    @Transactional
    public PeriodoProva registarRelatorio(FuncionarioId tutor, PeriodoProvaId id, PeriodoProva.Avaliacao avaliacao,
                                          String fundamentacao, LocalDate data) {
        var p = repository.findPeriodo(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Período de prova não encontrado."));
        p.registarRelatorio(tutor, avaliacao, fundamentacao, data);
        var gravado = repository.save(p);
        notificador.paraRh().tipo(TipoNotificacao.PERIODO_PROVA)
                .titulo("Relatório final do estágio de " + nome(p.getFuncionarioId()) + ": avaliação "
                        + (p.getAvaliacao() == PeriodoProva.Avaliacao.POSITIVA ? "positiva" : "negativa"))
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return gravado;
    }

    /**
     * A decisão no fim do período. Com sucesso, o estágio passa a nomeação definitiva ou contrato por tempo
     * indeterminado (um provimento novo, que se publica); sem sucesso, {@link #semSucesso}.
     */
    @Transactional
    public Resultado<PeriodoProva> concluir(FuncionarioId funcionarioId, PeriodoProvaId id, PeriodoProva.Avaliacao avaliacao,
                                            String fundamentacao, LocalDate data, String workerStateId) {
        var p = periodo(funcionarioId, id);
        p.concluir(avaliacao, fundamentacao, data != null ? data : hoje());
        repository.save(p);
        List<String> alertas = new ArrayList<>();
        var provimento = repository.findById(p.getProvimentoId()).orElseThrow();
        if (p.getEstado() == PeriodoProva.Estado.CONCLUIDO_COM_SUCESSO) {
            var seguinte = provimento.getModalidade().depoisDoEstagio();
            if (seguinte != null) {
                LocalDate inicio = p.getFimPrevisto().plusDays(1);
                var novo = repository.save(Provimento.registar(funcionarioId, seguinte, null, p.getDataFim(),
                        inicio.isBefore(p.getDataFim()) ? p.getDataFim() : inicio, provimento.getConcursoRef(), false,
                        provimento.getId(), "Depois do estágio probatório concluído com sucesso."));
                var dados = new LinkedHashMap<String, Object>();
                dados.put("modalidade", seguinte.name());
                dados.put("provimentoAnteriorId", provimento.getId().getStringValor());
                diarioFactos.registar(funcionarioId, TipoFactoRh.PROVIMENTO, novo.getDataPosse(), "PROVIMENTO",
                        novo.getId().getStringValor(), seguinte == ModalidadeProvimento.NOMEACAO_DEFINITIVA
                                ? "Nomeação definitiva depois do estágio probatório" : "Contrato por tempo indeterminado depois do estágio probatório",
                        dados);
                alertas.add("O tempo do estágio conta na carreira e categoria (art. 72.º n.º 5). Registe o despacho da "
                        + (seguinte == ModalidadeProvimento.NOMEACAO_DEFINITIVA ? "nomeação definitiva" : "celebração do contrato")
                        + " e a publicação.");
            }
        } else {
            alertas.addAll(semSucesso(provimento, p, workerStateId, "Estágio probatório concluído sem sucesso"));
        }
        avisar(p, p.getEstado() == PeriodoProva.Estado.CONCLUIDO_COM_SUCESSO
                ? "Concluiu com sucesso o seu período de prova" : "O seu período de prova terminou sem sucesso");
        return new Resultado<>(p, alertas);
    }

    /** Art. 72.º n.º 6 e art. 81.º n.º 1: por relatório (acto) fundamentado. */
    @Transactional
    public Resultado<PeriodoProva> cessarAntecipadamente(FuncionarioId funcionarioId, PeriodoProvaId id, String fundamentacao,
                                                         LocalDate data, String workerStateId) {
        var p = periodo(funcionarioId, id);
        p.cessarAntecipadamente(fundamentacao, data != null ? data : hoje());
        repository.save(p);
        var provimento = repository.findById(p.getProvimentoId()).orElseThrow();
        var alertas = semSucesso(provimento, p, workerStateId, p.getTipo() == PeriodoProva.Tipo.ESTAGIO_PROBATORIO
                ? "Cessação antecipada do estágio probatório" : "Cessação do contrato no período experimental");
        avisar(p, "O seu período de prova foi cessado antecipadamente");
        return new Resultado<>(p, alertas);
    }

    /** Art. 81.º n.º 2: o agente denuncia o contrato no período experimental, sem aviso prévio. */
    @Transactional
    public Resultado<PeriodoProva> denunciar(FuncionarioId funcionarioId, PeriodoProvaId id, LocalDate data, String workerStateId) {
        var p = periodo(funcionarioId, id);
        p.denunciar(data != null ? data : hoje());
        repository.save(p);
        var provimento = repository.findById(p.getProvimentoId()).orElseThrow();
        return new Resultado<>(p, semSucesso(provimento, p, workerStateId, "Denúncia do contrato no período experimental"));
    }

    /**
     * Sem sucesso: quem já era definitivo noutra carreira regressa a ela (arts. 57.º n.º 5, 72.º n.º 7) — o RH
     * recoloca-o; os outros saem: exoneração obrigatória no estágio (art. 94.º n.º 3), cessação do contrato no
     * período experimental (art. 81.º), pelo caminho único da cessação, no dia do fim.
     */
    List<String> semSucesso(Provimento provimento, PeriodoProva p, String workerStateId, String motivo) {
        List<String> alertas = new ArrayList<>();
        if (provimento.isVemDeOutraCarreira()) {
            alertas.add("O colaborador regressa à carreira e categoria de origem, onde o tempo do estágio conta. "
                    + "Recoloque-o pela mudança de carreira.");
            return alertas;
        }
        WorkerState estado = workerStateId != null && !workerStateId.isBlank() ? estado(workerStateId)
                : cessacaoService.estadoDeCessacaoPorOmissao();
        cessacaoService.cessar(p.getFuncionarioId(), estado, p.getDataFim(),
                p.getTipo() == PeriodoProva.Tipo.ESTAGIO_PROBATORIO ? "EXONERACAO_OBRIGATORIA" : "CESSACAO_PERIODO_EXPERIMENTAL",
                motivo + (p.getFundamentacao() != null ? ": " + p.getFundamentacao() : ""));
        alertas.add("O vínculo cessou a " + Datas.pt(p.getDataFim()) + " (" + motivo.toLowerCase() + ").");
        return alertas;
    }

    @Transactional(readOnly = true)
    public List<Provimento> provimentos(FuncionarioId funcionarioId) {
        return repository.findByFuncionario(funcionarioId);
    }

    @Transactional(readOnly = true)
    public List<PeriodoProva> periodos(FuncionarioId funcionarioId) {
        return repository.findPeriodosDoFuncionario(funcionarioId);
    }

    @Transactional(readOnly = true)
    public List<PeriodoProva> tutorias(FuncionarioId tutor) {
        return repository.findEmCursoDoTutor(tutor);
    }

    private void avisar(PeriodoProva p, String titulo) {
        notificador.para(p.getFuncionarioId()).tipo(TipoNotificacao.PERIODO_PROVA).titulo(titulo)
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
    }

    private String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    private PeriodoProva periodo(FuncionarioId funcionarioId, PeriodoProvaId id) {
        return repository.findPeriodo(id).filter(p -> p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Período de prova não encontrado."));
    }

    private WorkerState estado(String id) {
        try {
            var e = workerStateRepository.findById(WorkerStateId.from(UUID.fromString(id.trim())))
                    .orElseThrow(() -> invalido("O estado indicado não existe."));
            if (!e.isEndsEmployment()) throw invalido("O estado indicado não termina a relação de emprego.");
            return e;
        } catch (IllegalArgumentException ex) {
            throw invalido("O estado indicado não existe.");
        }
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
