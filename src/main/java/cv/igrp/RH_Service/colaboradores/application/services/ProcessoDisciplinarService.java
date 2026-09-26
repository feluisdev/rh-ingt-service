package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.ActoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.EspecieProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.PenaDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * <b>A tramitação do processo disciplinar</b> (Estatuto Disciplinar; BR-DIS-03..24): da participação à execução da pena.
 * Os actos regista-os o RH (ou o instrutor), cada um com a sua data; os prazos derivam deles. Os efeitos da pena
 * aplicam-se no dia em que são devidos (pelo job, ou ao notificar, se o dia já chegou): facto para o salarial, afastamento
 * para o apuramento, cessação do vínculo ou da comissão, publicação no BO.
 */
@Service
@RequiredArgsConstructor
public class ProcessoDisciplinarService {

    static final String RECURSO = "PROCESSO_DISCIPLINAR";

    /** O resultado de um acto, com o que o RH ainda deve saber. */
    public record Resultado(ProcessoDisciplinar processo, List<String> alertas) {}

    private final ProcessoDisciplinarRepository repository;
    private final FuncionarioRepository funcionarioRepository;
    private final CalendarioFeriadosService calendario;
    private final DiarioFactos diarioFactos;
    private final CessacaoService cessacaoService;
    private final WorkerStateRepository workerStateRepository;
    private final ComissaoServicoService comissaoServicoService;
    private final LicencaMobilidadeRepository licencaRepository;
    private final MobilidadeService mobilidadeService;
    private final PublicacoesService publicacoes;
    private final Notificador notificador;

    // ---------------------------------------------------------------- participação e instauração

    @Transactional
    public Resultado participar(FuncionarioId funcionarioId, String numero, EspecieProcessoDisciplinar especie, LocalDate dataInfraccao,
                                LocalDate dataParticipacao, String factos, PenaDisciplinar penaPrevista) {
        funcionario(funcionarioId);
        String n = numero != null && !numero.isBlank() ? numero.trim()
                : "PD/" + hoje().getYear() + "/" + String.format("%03d", repository.contarDoAno(hoje().getYear()) + 1);
        var p = repository.save(ProcessoDisciplinar.participar(funcionarioId, n, especie, dataInfraccao, dataParticipacao, factos,
                penaPrevista, hoje()));
        notificador.paraRh().tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                .titulo("Participação disciplinar " + n + " contra " + nome(funcionarioId) + ": decida se instaura o processo")
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return new Resultado(p, alertas(p));
    }

    @Transactional
    public Resultado instaurar(FuncionarioId funcionarioId, ProcessoDisciplinarId id, String despacho, LocalDate data, String entidade,
                               FuncionarioId instrutorId, String instrutorNome) {
        var p = processo(funcionarioId, id);
        p.instaurar(despacho, dia(data), entidade);
        if (instrutorId != null || (instrutorNome != null && !instrutorNome.isBlank())) nomear(p, instrutorId, instrutorNome, dia(data));
        return guardar(p);
    }

    @Transactional
    public Resultado nomearInstrutor(FuncionarioId funcionarioId, ProcessoDisciplinarId id, FuncionarioId instrutorId, String instrutorNome,
                                     LocalDate data) {
        var p = processo(funcionarioId, id);
        nomear(p, instrutorId, instrutorNome, dia(data));
        return guardar(p);
    }

    private void nomear(ProcessoDisciplinar p, FuncionarioId instrutorId, String nome, LocalDate data) {
        String n = nome;
        if (instrutorId != null) {
            var f = funcionarioRepository.findById(instrutorId)
                    .orElseThrow(() -> IgrpResponseStatusException.notFound("O instrutor indicado não existe."));
            if (!Boolean.TRUE.equals(f.getIsActive())) throw invalido("O instrutor indicado já não está ao serviço.");
            if (n == null || n.isBlank()) n = f.getNomeCompleto();
        }
        p.nomearInstrutor(instrutorId, n, data);
        if (instrutorId != null)
            notificador.para(instrutorId).tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                    .titulo("Foi nomeado instrutor do processo disciplinar " + p.getProcessNumber())
                    .texto("Inicie a instrução em 3 dias úteis (art. 48.º do Estatuto Disciplinar).")
                    .recurso(RECURSO, p.getId().getStringValor()).enviar();
    }

    // ---------------------------------------------------------------- instrução

    @Transactional
    public Resultado iniciarInstrucao(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data) {
        var p = processo(funcionarioId, id);
        p.iniciarInstrucao(dia(data));
        return guardar(p);
    }

    @Transactional
    public Resultado prorrogarInstrucao(FuncionarioId funcionarioId, ProcessoDisciplinarId id, Integer dias, LocalDate data) {
        var p = processo(funcionarioId, id);
        p.prorrogarInstrucao(dias, dia(data));
        return guardar(p);
    }

    /** A suspensão preventiva é facto para o salarial (com ou sem perda do vencimento de exercício) — BR-DIS-08. */
    @Transactional
    public Resultado suspenderPreventivamente(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate inicio, Integer dias,
                                              boolean perdaVencimento) {
        var p = processo(funcionarioId, id);
        var a = p.suspenderPreventivamente(dia(inicio), dias, perdaVencimento, hoje());
        var r = guardar(p);
        var dados = new LinkedHashMap<String, Object>();
        dados.put("processo", p.getProcessNumber());
        dados.put("ate", a.dataFim());
        dados.put("perdaVencimentoExercicio", perdaVencimento);
        diarioFactos.registar(funcionarioId, TipoFactoRh.SUSPENSAO_PREVENTIVA, a.data(), RECURSO, p.getId().getStringValor(),
                "Suspensão preventiva de " + Datas.pt(a.data()) + " a " + Datas.pt(a.dataFim()), dados);
        notificador.para(funcionarioId).tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                .titulo("Suspensão preventiva de " + Datas.pt(a.data()) + " a " + Datas.pt(a.dataFim()))
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return r;
    }

    @Transactional
    public Resultado levantarSuspensao(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data) {
        var p = processo(funcionarioId, id);
        p.levantarSuspensao(dia(data));
        var r = guardar(p);
        var dados = new LinkedHashMap<String, Object>();
        dados.put("processo", p.getProcessNumber());
        dados.put("evento", "LEVANTAMENTO");
        diarioFactos.registar(funcionarioId, TipoFactoRh.SUSPENSAO_PREVENTIVA, dia(data), RECURSO, p.getId().getStringValor(),
                "Levantamento da suspensão preventiva", dados);
        return r;
    }

    // ---------------------------------------------------------------- acusação, defesa, relatório, decisão

    @Transactional
    public Resultado acusar(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, PenaDisciplinar pena, String texto) {
        var p = processo(funcionarioId, id);
        p.acusar(dia(data), pena, texto);
        return guardar(p);
    }

    @Transactional
    public Resultado notificarAcusacao(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, Integer prazoDefesa,
                                       boolean complexo) {
        var p = processo(funcionarioId, id);
        p.notificarAcusacao(dia(data), prazoDefesa, complexo);
        var n = p.ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO).orElseThrow();
        notificador.para(funcionarioId).tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                .titulo("Foi notificado da acusação no processo " + p.getProcessNumber())
                .texto("Pode apresentar a sua defesa escrita até " + Datas.pt(n.dataFim()) + ".")
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        return guardar(p);
    }

    @Transactional
    public Resultado registarDefesa(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, String texto) {
        var p = processo(funcionarioId, id);
        p.registarDefesa(dia(data), texto);
        return guardar(p);
    }

    @Transactional
    public Resultado relatorio(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, PenaDisciplinar proposta,
                               Integer duracao, String texto) {
        var p = processo(funcionarioId, id);
        p.relatorio(dia(data), proposta, duracao, texto, hoje());
        return guardar(p);
    }

    @Transactional
    public Resultado decidir(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, PenaDisciplinar pena, Integer duracao,
                             String entidade, String fundamentacao, Integer suspensaoAnos) {
        var p = processo(funcionarioId, id);
        p.decidir(dia(data), pena, duracao, entidade, fundamentacao, comissaoEmCurso(funcionarioId, dia(data)).isPresent(), suspensaoAnos);
        return guardar(p);
    }

    /** A notificação da decisão; se a pena já é para executar (o dia seguinte chegou), executa-se já. */
    @Transactional
    public Resultado notificarDecisao(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data) {
        var p = processo(funcionarioId, id);
        p.notificarDecisao(dia(data));
        notificador.para(funcionarioId).tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                .titulo("Foi notificado da decisão do processo " + p.getProcessNumber())
                .texto(p.getPena() != null ? "Pena: " + p.getPenalty() + ". Pode recorrer até "
                        + Datas.pt(p.ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).orElseThrow().dataFim()) + "." : null)
                .recurso(RECURSO, p.getId().getStringValor()).enviar();
        if (p.efeitosDevidos(hoje())) executar(p);
        if (p.getPena() != null) caducarSuspensoes(p, dia(data));
        return guardar(p);
    }

    /** Punido de novo: as penas suspensas deste colaborador que ainda correm caducam e executam-se (art. 34.º n.º 4). */
    private void caducarSuspensoes(ProcessoDisciplinar novo, LocalDate data) {
        for (var outro : repository.findAllByFuncionarioId(novo.getFuncionarioId())) {
            if (outro.getId().equals(novo.getId()) || !outro.penaSuspensaEm(data)) continue;
            outro.caducarSuspensao(data, "Punido de novo no processo " + novo.getProcessNumber());
            if (outro.efeitosDevidos(hoje())) executar(outro);
            repository.save(outro);
            notificador.paraRh().tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                    .titulo("Caducou a suspensão da pena do processo " + outro.getProcessNumber() + " (" + nome(outro.getFuncionarioId()) + ")")
                    .texto("A pena executa-se a partir de " + Datas.pt(data.plusDays(1)) + " (art. 34.º n.º 4 do Estatuto Disciplinar).")
                    .recurso(RECURSO, outro.getId().getStringValor()).enviar();
        }
    }

    /** A reabilitação (art. 95.º): regista-se e publica-se no Boletim Oficial (n.º 7). */
    @Transactional
    public Resultado reabilitar(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, String despacho) {
        var p = processo(funcionarioId, id);
        p.reabilitar(dia(data), despacho);
        publicacoes.aPublicar(PublicacaoOficial.TipoActo.REABILITACAO, PublicacaoOficial.Meio.BOLETIM_OFICIAL, funcionarioId,
                RECURSO + "_REABILITACAO", p.getId().getStringValor(), nome(funcionarioId) + " — reabilitação (processo "
                        + p.getProcessNumber() + ", " + despacho.trim() + ")", dia(data));
        return guardar(p);
    }

    /**
     * A revisão procedente (arts. 90.º–94.º): revoga ou altera a pena, sem agravar. Se a pena já tinha sido executada, a
     * correcção vai ao diário; a de aposentação compulsiva ou demissão publica-se (art. 94.º n.º 7), e o RH é avisado de que
     * cabe prover o agente em lugar de categoria igual ou equivalente (art. 94.º n.º 4).
     */
    @Transactional
    public Resultado rever(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, ProcessoDisciplinar.ResultadoRevisao resultado,
                           PenaDisciplinar novaPena, Integer duracao, String despacho) {
        var p = processo(funcionarioId, id);
        var anterior = p.getPena();
        boolean executada = p.getEfeitosAplicadosEm() != null;
        p.rever(dia(data), resultado, novaPena, duracao, despacho);
        if (executada) {
            var dados = new LinkedHashMap<String, Object>();
            dados.put("processo", p.getProcessNumber());
            dados.put("evento", "REVISAO_" + resultado.name());
            dados.put("pena", p.getPena() != null ? p.getPena().name() : null);
            dados.put("duracao", p.getPenaDuracao());
            dados.put("fim", p.getPenaltyEndDate());
            diarioFactos.registar(funcionarioId, TipoFactoRh.PENA_DISCIPLINAR, dia(data), RECURSO, p.getId().getStringValor(),
                    "Revisão do processo disciplinar: " + p.getPenalty(), dados);
        }
        var r = guardar(p);
        var alertas = new ArrayList<>(r.alertas());
        if (anterior != null && anterior.publica()) {
            publicacoes.aPublicar(PublicacaoOficial.TipoActo.PENA_DISCIPLINAR, PublicacaoOficial.Meio.BOLETIM_OFICIAL, funcionarioId,
                    RECURSO + "_REVISAO", p.getId().getStringValor(), nome(funcionarioId) + " — revisão procedente do processo "
                            + p.getProcessNumber() + ": " + p.getPenalty(), dia(data));
            if (executada && (p.getPena() == null || !p.getPena().expulsiva()))
                alertas.add("Cabe prover o agente em lugar de categoria igual ou equivalente, ou na primeira vaga, em disponibilidade entretanto (art. 94.º n.º 4).");
        }
        return new Resultado(r.processo(), alertas);
    }

    @Transactional
    public Resultado interporRecurso(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, String texto) {
        var p = processo(funcionarioId, id);
        p.interporRecurso(dia(data), texto);
        return guardar(p);
    }

    /** A decisão do recurso. Se a pena já tinha sido executada, a anulação ou a diminuição vão ao diário de factos. */
    @Transactional
    public Resultado decidirRecurso(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data,
                                    ProcessoDisciplinar.ResultadoRecurso resultado, PenaDisciplinar pena, Integer duracao) {
        var p = processo(funcionarioId, id);
        boolean jaExecutada = p.getEfeitosAplicadosEm() != null;
        p.decidirRecurso(dia(data), resultado, pena, duracao);
        if (jaExecutada && resultado != ProcessoDisciplinar.ResultadoRecurso.MANTIDA) {
            var dados = new LinkedHashMap<String, Object>();
            dados.put("processo", p.getProcessNumber());
            dados.put("evento", resultado.name());
            dados.put("pena", p.getPena() != null ? p.getPena().name() : null);
            dados.put("duracao", p.getPenaDuracao());
            dados.put("fim", p.getPenaltyEndDate());
            diarioFactos.registar(funcionarioId, TipoFactoRh.PENA_DISCIPLINAR, dia(data), RECURSO, p.getId().getStringValor(),
                    resultado == ProcessoDisciplinar.ResultadoRecurso.ANULADA ? "Pena anulada em recurso" : "Pena diminuída em recurso: " + p.getPenalty(),
                    dados);
        }
        if (p.efeitosDevidos(hoje())) executar(p);
        return guardar(p);
    }

    @Transactional
    public Resultado arquivar(FuncionarioId funcionarioId, ProcessoDisciplinarId id, LocalDate data, String motivo) {
        var p = processo(funcionarioId, id);
        p.arquivar(dia(data), motivo);
        return guardar(p);
    }

    // ---------------------------------------------------------------- execução (BR-DIS-15..19)

    /**
     * Os efeitos da pena (art. 17.º), na data de execução: o facto PENA_DISCIPLINAR (o salarial desconta a multa e os dias
     * de suspensão); a suspensão e a inactividade afastam o agente (os dias saem do apuramento); a aposentação compulsiva e
     * a demissão cessam o vínculo e publicam-se no BO (art. 15.º n.º 2); a cessação da comissão — e a acessória, a quem
     * está em comissão e é punido com multa ou mais (art. 29.º n.º 2) — faz regressar o dirigente.
     */
    void executar(ProcessoDisciplinar p) {
        LocalDate data = p.dataExecucao();
        var pena = p.getPena();
        var fid = p.getFuncionarioId();
        List<String> feito = new ArrayList<>();
        var dados = new LinkedHashMap<String, Object>();
        dados.put("processo", p.getProcessNumber());
        dados.put("pena", pena.name());
        dados.put("duracao", p.getPenaDuracao());
        dados.put("inicio", p.getPenaltyStartDate());
        dados.put("fim", p.getPenaltyEndDate());
        diarioFactos.registar(fid, TipoFactoRh.PENA_DISCIPLINAR, data, RECURSO, p.getId().getStringValor(),
                "Pena disciplinar: " + p.getPenalty(), dados);
        feito.add("Facto da pena no diário");
        String motivo = "Pena de " + pena.nome() + " (processo " + p.getProcessNumber() + ")";
        if (pena == PenaDisciplinar.APOSENTACAO_COMPULSIVA || pena == PenaDisciplinar.DEMISSAO) {
            WorkerState estado = pena == PenaDisciplinar.APOSENTACAO_COMPULSIVA
                    ? workerStateRepository.findByCode("RETIRED").filter(WorkerState::isEndsEmployment).orElse(cessacaoService.estadoDeCessacaoPorOmissao())
                    : cessacaoService.estadoDeCessacaoPorOmissao();
            cessacaoService.cessar(fid, estado, data, "PENA_DISCIPLINAR", motivo);
            feito.add("Vínculo cessado");
        }
        if (pena.publica()) {
            publicacoes.aPublicar(PublicacaoOficial.TipoActo.PENA_DISCIPLINAR, PublicacaoOficial.Meio.BOLETIM_OFICIAL, fid, RECURSO,
                    p.getId().getStringValor(), nome(fid) + " — " + motivo, data);
            feito.add("Publicação no Boletim Oficial");
        }
        boolean cessaComissao = pena == PenaDisciplinar.CESSACAO_COMISSAO
                || (pena.pelomenos(PenaDisciplinar.MULTA) && pena != PenaDisciplinar.APOSENTACAO_COMPULSIVA && pena != PenaDisciplinar.DEMISSAO);
        if (cessaComissao) {
            comissaoEmCurso(fid, data).ifPresent(c -> {
                comissaoServicoService.cessar(fid, c.getId(), ComissaoServicoService.Iniciativa.PENA_DISCIPLINAR, data, data,
                        pena == PenaDisciplinar.CESSACAO_COMISSAO ? motivo : motivo + " — cessação acessória (art. 29.º n.º 2)");
                feito.add("Comissão de serviço cessada");
            });
        }
        p.marcarEfeitosAplicados(hoje(), LocalDateTime.now(), String.join("; ", feito) + ".");
        notificador.paraRh().tipo(TipoNotificacao.PROCESSO_DISCIPLINAR)
                .titulo("Pena executada no processo " + p.getProcessNumber() + " (" + nome(fid) + "): " + p.getPenalty())
                .texto(String.join("; ", feito) + ".").recurso(RECURSO, p.getId().getStringValor()).enviar();
    }

    /** Para o job: executa as penas cujo dia chegou e conclui os processos transitados. */
    @Transactional
    public int executarDevidas(LocalDate dia) {
        int n = 0;
        for (var p : repository.findComPenaPorExecutar()) {
            if (p.efeitosDevidos(dia)) {
                executar(p);
                repository.save(p);
                n++;
            }
        }
        for (var p : repository.findEmCurso())
            if (p.concluirSeTransitado(dia)) repository.save(p);
        return n;
    }

    /** Para o job: avisa dos prazos que terminam daqui a 2 dias e dos que terminaram ontem (BR-DIS-05). */
    @Transactional
    public int avisarPrazos(LocalDate dia) {
        int avisos = 0;
        for (var p : repository.findEmCurso()) {
            for (var prazo : prazos(p, dia)) {
                String quando = prazo.data().equals(dia.plusDays(2)) ? "termina a " + Datas.pt(prazo.data())
                        : prazo.data().equals(dia.minusDays(1)) ? "terminou ontem" : null;
                if (quando == null) continue;
                String titulo = "Processo " + p.getProcessNumber() + " (" + nome(p.getFuncionarioId()) + "): " + prazo.nome() + " " + quando;
                notificador.paraRh().tipo(TipoNotificacao.PROCESSO_DISCIPLINAR).titulo(titulo).recurso(RECURSO, p.getId().getStringValor()).enviar();
                if (p.getInstrutorId() != null)
                    notificador.para(p.getInstrutorId()).tipo(TipoNotificacao.PROCESSO_DISCIPLINAR).titulo(titulo)
                            .recurso(RECURSO, p.getId().getStringValor()).enviar();
                avisos++;
            }
        }
        return avisos;
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public ProcessoDisciplinar ver(FuncionarioId funcionarioId, ProcessoDisciplinarId id) {
        return processo(funcionarioId, id);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDisciplinar> emCurso() {
        return repository.findEmCurso();
    }

    /** Os prazos do processo, com os dias úteis pelo calendário de feriados do arguido. */
    public List<ProcessoDisciplinar.Prazo> prazos(ProcessoDisciplinar p, LocalDate hoje) {
        return p.prazos(hoje, util(p.getFuncionarioId(), hoje));
    }

    /** O que o RH deve saber: a prescrição, a suspensão preventiva em curso, os prazos já passados. */
    public List<String> alertas(ProcessoDisciplinar p) {
        var l = new ArrayList<String>();
        LocalDate hoje = hoje();
        var prescricao = p.prescreveEm();
        if (prescricao != null && p.getFase() != null && p.getFase().antesDaDecisao()) {
            LocalDate instauracao = p.ultimo(ActoDisciplinar.Tipo.INSTAURACAO).map(ActoDisciplinar::data).orElse(null);
            if (instauracao != null && instauracao.isAfter(prescricao))
                l.add("O procedimento prescreveu a " + Datas.pt(prescricao) + ", antes da instauração: não se pode punir (art. 6.º).");
            else if (instauracao == null)
                l.add("O procedimento prescreve a " + Datas.pt(prescricao) + " se não for instaurado antes (art. 6.º).");
        }
        for (var prazo : prazos(p, hoje))
            if (prazo.vencido()) l.add(prazo.nome() + ": o prazo terminou a " + Datas.pt(prazo.data()) + ".");
        return l;
    }

    public String nome(FuncionarioId id) {
        return funcionarioRepository.findById(id).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }

    private Resultado guardar(ProcessoDisciplinar p) {
        var gravado = repository.save(p);
        return new Resultado(gravado, alertas(gravado));
    }

    private Optional<LicencaMobilidade> comissaoEmCurso(FuncionarioId funcionarioId, LocalDate data) {
        return licencaRepository.findActiveByFuncionarioIdAt(funcionarioId, data).stream()
                .filter(l -> mobilidadeService.subtipoSeExistir(l).map(s -> s.regressaOuCessa()).orElse(false))
                .findFirst();
    }

    private Predicate<LocalDate> util(FuncionarioId funcionarioId, LocalDate hoje) {
        Set<LocalDate> feriados = calendario.feriadosDoColaborador(funcionarioId, hoje.minusYears(1), hoje.plusYears(1));
        return d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(d);
    }

    private ProcessoDisciplinar processo(FuncionarioId funcionarioId, ProcessoDisciplinarId id) {
        return repository.findById(id).filter(p -> p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Processo disciplinar não encontrado."));
    }

    private Funcionario funcionario(FuncionarioId id) {
        return funcionarioRepository.findById(id).orElseThrow(() -> IgrpResponseStatusException.notFound("Colaborador não encontrado."));
    }

    private LocalDate dia(LocalDate data) {
        return data != null ? data : hoje();
    }

    LocalDate hoje() { return LocalDate.now(); }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
