package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * <b>O processo disciplinar</b> (Estatuto Disciplinar — Lei n.º 31/III/87 e DL n.º 8/97; BR-DIS). Os processos antigos são
 * só um registo (número, datas, pena em texto, BO, notas) e continuam a editar-se como antes; os novos têm
 * <b>tramitação</b>: fase, actos datados e os prazos que deles resultam. Os campos do registo continuam preenchidos pela
 * tramitação (o front antigo vê a pena, as datas e o BO como sempre).
 *
 * <p>Prazos (dias seguidos, salvo «úteis»): início da instrução em 3 dias úteis e instrução em 30, prorrogável uma vez
 * até 30 — 15 se não se fixar (art. 48.º); acusação ou relatório em 5 dias úteis (art. 60.º); notificação da acusação em
 * 48 horas e defesa em 10 a 20 dias, até 45 se complexo, até 5 nos sumários (arts. 62.º, 78.º); relatório final em 10
 * dias (art. 71.º); decisão em 15 dias úteis (art. 72.º); recurso hierárquico em 15 dias (art. 84.º).
 */
@Getter
public class ProcessoDisciplinar {

    /** Um prazo do processo, e se já passou. */
    public record Prazo(String nome, LocalDate data, boolean vencido) {}

    /** Art. 48.º n.º 1 e 2. */
    public static final int DIAS_INSTRUCAO = 30;
    public static final int MAX_PRORROGACAO = 30;
    public static final int PRORROGACAO_POR_OMISSAO = 15;
    /** Art. 56.º n.º 1. */
    public static final int MAX_SUSPENSAO_PREVENTIVA = 90;
    /** Art. 84.º n.º 1. */
    public static final int DIAS_RECURSO = 15;

    private ProcessoDisciplinarId id;
    private FuncionarioId funcionarioId;
    private String processNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String penalty;
    private LocalDate penaltyStartDate;
    private LocalDate penaltyEndDate;
    private String officialBulletin;
    private String notes;

    // ---------------------------------------------------------------- tramitação (nula nos registos antigos)
    private EspecieProcessoDisciplinar especie;
    private FaseProcessoDisciplinar fase;
    private LocalDate dataInfraccao;
    private PenaDisciplinar penaPrevista;
    private FuncionarioId instrutorId;
    private String instrutorNome;
    private PenaDisciplinar pena;
    private Integer penaDuracao;
    private LocalDateTime efeitosAplicadosEm;
    private List<ActoDisciplinar> actos = new ArrayList<>();

    private ProcessoDisciplinar() {}

    /** O registo antigo, sem tramitação. */
    public static ProcessoDisciplinar criar(FuncionarioId funcionarioId, String processNumber,
                                             LocalDate startDate, LocalDate endDate, String penalty,
                                             LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                                             String officialBulletin, String notes) {
        ProcessoDisciplinar p = new ProcessoDisciplinar();
        p.id = ProcessoDisciplinarId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.processNumber = processNumber;
        p.startDate = startDate;
        p.endDate = endDate;
        p.penalty = penalty;
        p.penaltyStartDate = penaltyStartDate;
        p.penaltyEndDate = penaltyEndDate;
        p.officialBulletin = officialBulletin;
        p.notes = notes;
        return p;
    }

    public static ProcessoDisciplinar reconstituir(ProcessoDisciplinarId id, FuncionarioId funcionarioId,
                                                    String processNumber, LocalDate startDate, LocalDate endDate,
                                                    String penalty, LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                                                    String officialBulletin, String notes) {
        return reconstituir(id, funcionarioId, processNumber, startDate, endDate, penalty, penaltyStartDate, penaltyEndDate,
                officialBulletin, notes, null, null, null, null, null, null, null, null, null, List.of());
    }

    public static ProcessoDisciplinar reconstituir(ProcessoDisciplinarId id, FuncionarioId funcionarioId,
                                                    String processNumber, LocalDate startDate, LocalDate endDate,
                                                    String penalty, LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                                                    String officialBulletin, String notes, EspecieProcessoDisciplinar especie,
                                                    FaseProcessoDisciplinar fase, LocalDate dataInfraccao, PenaDisciplinar penaPrevista,
                                                    FuncionarioId instrutorId, String instrutorNome, PenaDisciplinar pena,
                                                    Integer penaDuracao, LocalDateTime efeitosAplicadosEm, List<ActoDisciplinar> actos) {
        ProcessoDisciplinar p = new ProcessoDisciplinar();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.processNumber = processNumber;
        p.startDate = startDate;
        p.endDate = endDate;
        p.penalty = penalty;
        p.penaltyStartDate = penaltyStartDate;
        p.penaltyEndDate = penaltyEndDate;
        p.officialBulletin = officialBulletin;
        p.notes = notes;
        p.especie = especie;
        p.fase = fase;
        p.dataInfraccao = dataInfraccao;
        p.penaPrevista = penaPrevista;
        p.instrutorId = instrutorId;
        p.instrutorNome = instrutorNome;
        p.pena = pena;
        p.penaDuracao = penaDuracao;
        p.efeitosAplicadosEm = efeitosAplicadosEm;
        p.actos = new ArrayList<>(actos);
        p.actos.sort(Comparator.comparing(ActoDisciplinar::data));
        return p;
    }

    /**
     * O registo antigo edita-se como antes. Num processo com tramitação, a pena e as datas são da tramitação: mudá-las
     * aqui dá 409 (o número, o BO e as notas continuam livres).
     */
    public void atualizar(String processNumber, LocalDate startDate, LocalDate endDate, String penalty,
                          LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                          String officialBulletin, String notes) {
        if (fase != null && (muda(startDate, this.startDate) || muda(endDate, this.endDate) || muda(penalty, this.penalty)
                || muda(penaltyStartDate, this.penaltyStartDate) || muda(penaltyEndDate, this.penaltyEndDate)))
            throw IgrpResponseStatusException.conflict("Este processo tem tramitação: a pena e as datas mudam pelos actos do processo.");
        if (processNumber != null) this.processNumber = processNumber;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (penalty != null) this.penalty = penalty;
        if (penaltyStartDate != null) this.penaltyStartDate = penaltyStartDate;
        if (penaltyEndDate != null) this.penaltyEndDate = penaltyEndDate;
        if (officialBulletin != null) this.officialBulletin = officialBulletin;
        if (notes != null) this.notes = notes;
    }

    private static boolean muda(Object novo, Object actual) {
        return novo != null && !novo.equals(actual);
    }

    public List<ActoDisciplinar> getActos() {
        return Collections.unmodifiableList(actos);
    }

    // ---------------------------------------------------------------- participação e instauração

    /** A participação, queixa ou auto (art. 47.º): abre o processo, ainda por instaurar (BR-DIS-03). */
    public static ProcessoDisciplinar participar(FuncionarioId funcionarioId, String numero, EspecieProcessoDisciplinar especie,
                                                 LocalDate dataInfraccao, LocalDate dataParticipacao, String factos,
                                                 PenaDisciplinar penaPrevista, LocalDate hoje) {
        if (funcionarioId == null) throw invalido("Indique o colaborador.");
        LocalDate data = dataParticipacao != null ? dataParticipacao : hoje;
        if (data.isAfter(hoje)) throw invalido("A data da participação não pode ser no futuro.");
        if (dataInfraccao != null && dataInfraccao.isAfter(data)) throw invalido("A infracção não pode ser depois da participação.");
        if (factos == null || factos.isBlank()) throw invalido("Descreva os factos participados.");
        var p = new ProcessoDisciplinar();
        p.id = ProcessoDisciplinarId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.processNumber = numero;
        p.especie = especie != null ? especie : EspecieProcessoDisciplinar.DISCIPLINAR_COMUM;
        p.fase = FaseProcessoDisciplinar.PARTICIPADO;
        p.dataInfraccao = dataInfraccao;
        p.penaPrevista = penaPrevista;
        p.startDate = data;
        p.notes = factos.trim();
        p.actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.PARTICIPACAO, data, factos.trim()));
        return p;
    }

    /** O despacho liminar que instaura (art. 50.º); pode já nomear o instrutor (art. 51.º). */
    public void instaurar(String despacho, LocalDate data, String entidade) {
        exigir(FaseProcessoDisciplinar.PARTICIPADO, "instaurar");
        data = naoAntes(data, ActoDisciplinar.Tipo.PARTICIPACAO, "a participação");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho que instaura o processo.");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.INSTAURACAO, data,
                despacho.trim() + (entidade != null && !entidade.isBlank() ? " (" + entidade.trim() + ")" : "")));
        this.startDate = data;
        this.fase = FaseProcessoDisciplinar.INSTAURADO;
    }

    /** Nomear (ou substituir, por impedimento ou suspeição — arts. 54.º e 55.º) o instrutor. Nunca o próprio arguido. */
    public void nomearInstrutor(FuncionarioId instrutor, String nome, LocalDate data) {
        if (fase != FaseProcessoDisciplinar.INSTAURADO && fase != FaseProcessoDisciplinar.EM_INSTRUCAO && fase != FaseProcessoDisciplinar.ACUSADO)
            throw IgrpResponseStatusException.conflict("O instrutor nomeia-se depois de instaurado o processo e antes do relatório.");
        if (instrutor == null && (nome == null || nome.isBlank())) throw invalido("Indique o instrutor.");
        if (funcionarioId.equals(instrutor)) throw invalido("O arguido não pode ser o instrutor do seu processo.");
        data = naoAntes(data, ActoDisciplinar.Tipo.INSTAURACAO, "a instauração");
        this.instrutorId = instrutor;
        this.instrutorNome = nome != null && !nome.isBlank() ? nome.trim() : null;
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.NOMEACAO_INSTRUTOR, data, this.instrutorNome));
    }

    // ---------------------------------------------------------------- instrução

    public void iniciarInstrucao(LocalDate data) {
        exigir(FaseProcessoDisciplinar.INSTAURADO, "iniciar a instrução");
        if (ultimo(ActoDisciplinar.Tipo.NOMEACAO_INSTRUTOR).isEmpty()) throw invalido("Nomeie primeiro o instrutor.");
        data = naoAntes(data, ActoDisciplinar.Tipo.NOMEACAO_INSTRUTOR, "a nomeação do instrutor");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.INICIO_INSTRUCAO, data, null));
        this.fase = FaseProcessoDisciplinar.EM_INSTRUCAO;
    }

    /** Uma única prorrogação, até 30 dias; sem dias fixados, 15 (art. 48.º n.os 1 e 2). */
    public void prorrogarInstrucao(Integer dias, LocalDate data) {
        exigir(FaseProcessoDisciplinar.EM_INSTRUCAO, "prorrogar a instrução");
        if (ultimo(ActoDisciplinar.Tipo.PRORROGACAO_INSTRUCAO).isPresent())
            throw IgrpResponseStatusException.conflict("A instrução só se prorroga uma vez (art. 48.º n.º 1).");
        int d = dias != null ? dias : PRORROGACAO_POR_OMISSAO;
        if (d < 1 || d > MAX_PRORROGACAO) throw invalido("A prorrogação vai de 1 a 30 dias.");
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.PRORROGACAO_INSTRUCAO, data, null, d, null, null, null));
    }

    /**
     * Suspensão preventiva (art. 56.º): durante a instrução e até à decisão, só se a infracção for punível com suspensão ou
     * mais, até 90 dias no total; com ou sem perda do vencimento de exercício (BR-DIS-08).
     */
    public ActoDisciplinar suspenderPreventivamente(LocalDate inicio, Integer dias, boolean perdaVencimento, LocalDate hoje) {
        if (fase == null || fase == FaseProcessoDisciplinar.PARTICIPADO || !fase.antesDaDecisao())
            throw IgrpResponseStatusException.conflict("A suspensão preventiva é durante a instrução e até à decisão.");
        if (penaPrevista == null || !penaPrevista.pelomenos(PenaDisciplinar.SUSPENSAO) || penaPrevista == PenaDisciplinar.CESSACAO_COMISSAO)
            throw invalido("A suspensão preventiva só cabe em infracção punível com suspensão ou pena mais grave (art. 56.º n.º 2).");
        if (inicio == null) inicio = hoje;
        if (dias == null || dias < 1) throw invalido("Indique quantos dias dura a suspensão preventiva.");
        int usados = suspensoes().stream().mapToInt(a -> (int) (a.dataFim().toEpochDay() - a.data().toEpochDay() + 1)).sum();
        if (usados + dias > MAX_SUSPENSAO_PREVENTIVA)
            throw invalido("A suspensão preventiva não passa de 90 dias no total (já tem " + usados + ").");
        LocalDate i = inicio;
        if (suspensoes().stream().anyMatch(a -> !i.isAfter(a.dataFim()) && !i.plusDays(dias - 1).isBefore(a.data())))
            throw invalido("Já há uma suspensão preventiva nesse período.");
        var a = new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.SUSPENSAO_PREVENTIVA, inicio, inicio.plusDays(dias - 1),
                dias, null, null, perdaVencimento ? "Com perda do vencimento de exercício" : "Sem perda de vencimento");
        actos.add(a);
        return a;
    }

    /** Levantar a suspensão preventiva antes do fim: o último dia suspenso é a véspera. */
    public void levantarSuspensao(LocalDate data) {
        var s = suspensoes().stream().filter(a -> !data.isAfter(a.dataFim()) && data.isAfter(a.data())).findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.conflict("Não há suspensão preventiva em curso nessa data."));
        actos.remove(s);
        actos.add(new ActoDisciplinar(s.id(), s.tipo(), s.data(), data.minusDays(1), s.dias(), s.pena(), s.duracao(), s.texto()));
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.LEVANTAMENTO_SUSPENSAO, data, null));
    }

    public List<ActoDisciplinar> suspensoes() {
        return actos.stream().filter(a -> a.tipo() == ActoDisciplinar.Tipo.SUSPENSAO_PREVENTIVA).toList();
    }

    // ---------------------------------------------------------------- acusação e defesa

    /**
     * A acusação (arts. 60.º n.º 2 e 61.º), com a pena aplicável. Nos processos sumários (arts. 78.º, 82.º) vem logo depois
     * da instauração, sem instrução. Inquéritos e sindicâncias não acusam.
     */
    public void acusar(LocalDate data, PenaDisciplinar penaAplicavel, String texto) {
        if (especie.semArguido()) throw invalido("Um inquérito, sindicância ou averiguação não acusa: termina no relatório.");
        boolean sumarioSemInstrucao = especie.sumario() && fase == FaseProcessoDisciplinar.INSTAURADO;
        if (fase != FaseProcessoDisciplinar.EM_INSTRUCAO && !sumarioSemInstrucao)
            throw IgrpResponseStatusException.conflict("A acusação deduz-se no fim da instrução.");
        if (penaAplicavel == null) throw invalido("A acusação indica a pena aplicável (art. 61.º n.º 1).");
        if (texto == null || texto.isBlank()) throw invalido("Descreva as infracções de que acusa.");
        data = naoAntes(data, sumarioSemInstrucao ? ActoDisciplinar.Tipo.INSTAURACAO : ActoDisciplinar.Tipo.INICIO_INSTRUCAO, "a instrução");
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.ACUSACAO, data, null, null, penaAplicavel, null, texto.trim()));
        this.penaPrevista = penaAplicavel;
        this.fase = FaseProcessoDisciplinar.ACUSADO;
    }

    /**
     * A notificação da acusação marca o prazo de defesa (art. 62.º): 10 a 20 dias (20 por omissão), até 45 se o processo
     * for complexo; nos sumários, até 5 (art. 78.º n.º 3).
     */
    public void notificarAcusacao(LocalDate data, Integer prazoDefesa, boolean complexo) {
        exigir(FaseProcessoDisciplinar.ACUSADO, "notificar a acusação");
        if (ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO).isPresent()) throw IgrpResponseStatusException.conflict("A acusação já foi notificada.");
        data = naoAntes(data, ActoDisciplinar.Tipo.ACUSACAO, "a acusação");
        int dias;
        if (especie.sumario()) {
            dias = prazoDefesa != null ? prazoDefesa : 5;
            if (dias < 1 || dias > 5) throw invalido("Neste processo a defesa tem até 5 dias (art. 78.º n.º 3).");
        } else {
            dias = prazoDefesa != null ? prazoDefesa : 20;
            int max = complexo ? 45 : 20;
            if (dias < 10 || dias > max)
                throw invalido("O prazo de defesa vai de 10 a " + max + " dias (art. 62.º" + (complexo ? " n.º 2" : " n.º 1") + ").");
        }
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO, data, data.plusDays(dias),
                dias, null, null, complexo ? "Processo complexo" : null));
    }

    public void registarDefesa(LocalDate data, String texto) {
        exigir(FaseProcessoDisciplinar.ACUSADO, "registar a defesa");
        var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO)
                .orElseThrow(() -> IgrpResponseStatusException.conflict("A acusação ainda não foi notificada."));
        if (ultimo(ActoDisciplinar.Tipo.DEFESA).isPresent()) throw IgrpResponseStatusException.conflict("A defesa já foi registada.");
        if (data == null) throw invalido("Indique a data em que a defesa foi apresentada.");
        if (data.isBefore(n.data())) throw invalido("A defesa não pode ser antes da notificação da acusação.");
        if (data.isAfter(n.dataFim()))
            throw invalido("O prazo de defesa terminou a " + Datas.pt(n.dataFim()) + ": a falta de resposta vale como audiência (art. 69.º).");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.DEFESA, data, texto));
    }

    // ---------------------------------------------------------------- relatório e decisão

    /**
     * O relatório final do instrutor (arts. 60.º n.º 1 e 71.º): propõe a pena ou o arquivamento ({@code proposta} nula).
     * Depois da acusação, só com a defesa apresentada ou o prazo de defesa passado.
     */
    public void relatorio(LocalDate data, PenaDisciplinar proposta, Integer duracao, String texto, LocalDate hoje) {
        if (fase == FaseProcessoDisciplinar.ACUSADO) {
            var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO)
                    .orElseThrow(() -> IgrpResponseStatusException.conflict("A acusação ainda não foi notificada ao arguido."));
            if (ultimo(ActoDisciplinar.Tipo.DEFESA).isEmpty() && !hoje.isAfter(n.dataFim()))
                throw IgrpResponseStatusException.conflict("O prazo de defesa corre até " + Datas.pt(n.dataFim()) + ".");
        } else if (fase != FaseProcessoDisciplinar.EM_INSTRUCAO) {
            throw IgrpResponseStatusException.conflict("O relatório final faz-se no fim da instrução ou depois da defesa.");
        } else if (proposta != null && !especie.semArguido()) {
            throw invalido("Sem acusação, o relatório só pode propor o arquivamento (art. 60.º n.º 1).");
        }
        if (proposta != null) validarPena(proposta, duracao);
        if (texto == null || texto.isBlank()) throw invalido("Escreva a síntese do relatório.");
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.RELATORIO, data != null ? data : hoje, null, null,
                proposta, duracao, texto.trim()));
        this.fase = FaseProcessoDisciplinar.RELATORIO;
    }

    /**
     * A decisão (art. 72.º): uma pena, ou nenhuma (arquiva). A pena pode ser mais grave que a proposta (n.º 2); quando não
     * concorda com o relatório, a decisão é fundamentada (art. 74.º). Um procedimento prescrito não pune (art. 6.º).
     */
    public void decidir(LocalDate data, PenaDisciplinar penaAplicada, Integer duracao, String entidade, String fundamentacao,
                        boolean temComissaoEmCurso) {
        decidir(data, penaAplicada, duracao, entidade, fundamentacao, temComissaoEmCurso, null);
    }

    /**
     * Com {@code suspensaoAnos}, a pena fica suspensa por 1 a 3 anos contados da notificação (art. 34.º): só a multa e a
     * suspensão; na censura escrita, suspende-se o registo.
     */
    public void decidir(LocalDate data, PenaDisciplinar penaAplicada, Integer duracao, String entidade, String fundamentacao,
                        boolean temComissaoEmCurso, Integer suspensaoAnos) {
        exigir(FaseProcessoDisciplinar.RELATORIO, "decidir");
        if (suspensaoAnos != null) {
            if (penaAplicada != PenaDisciplinar.MULTA && penaAplicada != PenaDisciplinar.SUSPENSAO && penaAplicada != PenaDisciplinar.CENSURA_ESCRITA)
                throw invalido("Só a multa e a suspensão se suspendem (e o registo da censura escrita) — art. 34.º.");
            if (suspensaoAnos < 1 || suspensaoAnos > 3) throw invalido("A suspensão da pena é de 1 a 3 anos (art. 34.º n.º 2).");
        }
        var rel = ultimo(ActoDisciplinar.Tipo.RELATORIO).orElseThrow();
        data = naoAntes(data, ActoDisciplinar.Tipo.RELATORIO, "o relatório");
        boolean concorda = Objects.equals(rel.pena(), penaAplicada) && Objects.equals(rel.duracao(), duracao);
        if (!concorda && (fundamentacao == null || fundamentacao.isBlank()))
            throw invalido("A decisão não concorda com o relatório: tem de ser fundamentada (art. 74.º).");
        if (penaAplicada != null) {
            if (especie.semArguido()) throw invalido("Um inquérito ou sindicância não aplica penas: instaure o processo disciplinar.");
            validarPena(penaAplicada, duracao);
            if (penaAplicada == PenaDisciplinar.CESSACAO_COMISSAO && !temComissaoEmCurso)
                throw invalido("A cessação da comissão aplica-se a quem está em comissão de serviço.");
            LocalDate instauracao = ultimo(ActoDisciplinar.Tipo.INSTAURACAO).map(ActoDisciplinar::data).orElse(startDate);
            if (dataInfraccao != null && instauracao.isAfter(penaAplicada.prescricao(dataInfraccao)))
                throw invalido("O procedimento prescreveu a " + Datas.pt(penaAplicada.prescricao(dataInfraccao))
                        + ", antes da instauração (art. 6.º): não se pode punir.");
        }
        String texto = (entidade != null && !entidade.isBlank() ? entidade.trim() + ": " : "")
                + (fundamentacao != null && !fundamentacao.isBlank() ? fundamentacao.trim() : "Concorda com o relatório.");
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.DECISAO, data, null, suspensaoAnos, penaAplicada, duracao,
                suspensaoAnos != null ? texto + " Pena suspensa por " + suspensaoAnos + " ano(s)." : texto));
        this.endDate = data;
        if (penaAplicada == null) {
            this.fase = FaseProcessoDisciplinar.ARQUIVADO;
            this.penalty = null;
            return;
        }
        this.pena = penaAplicada;
        this.penaDuracao = duracao;
        this.penalty = rotulo(penaAplicada, duracao) + (suspensaoAnos != null ? " — suspensa por " + suspensaoAnos + " ano(s)" : "");
        this.fase = FaseProcessoDisciplinar.DECIDIDO;
    }

    /** A notificação da decisão: a pena produz efeitos no dia seguinte (art. 77.º). */
    public void notificarDecisao(LocalDate data) {
        exigir(FaseProcessoDisciplinar.DECIDIDO, "notificar a decisão");
        data = naoAntes(data, ActoDisciplinar.Tipo.DECISAO, "a decisão");
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO, data, data.plusDays(DIAS_RECURSO),
                null, null, null, null));
        definirPeriodo(data.plusDays(1));
        if (suspensaAnos() != null) {
            this.penaltyStartDate = null;
            this.penaltyEndDate = null;
        }
        this.fase = FaseProcessoDisciplinar.NOTIFICADO;
    }

    // ---------------------------------------------------------------- recurso

    /** O recurso hierárquico, em 15 dias da notificação (art. 84.º n.º 1); suspende a execução (n.º 4). */
    public void interporRecurso(LocalDate data, String texto) {
        exigir(FaseProcessoDisciplinar.NOTIFICADO, "interpor recurso");
        var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).orElseThrow();
        if (data == null || data.isBefore(n.data())) throw invalido("Indique a data do recurso, depois da notificação.");
        if (data.isAfter(n.dataFim())) throw invalido("O prazo de recurso terminou a " + Datas.pt(n.dataFim()) + " (art. 84.º n.º 1).");
        if (ultimo(ActoDisciplinar.Tipo.RECURSO).isPresent()) throw IgrpResponseStatusException.conflict("Já foi interposto recurso.");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.RECURSO, data, texto));
        this.fase = FaseProcessoDisciplinar.EM_RECURSO;
    }

    public enum ResultadoRecurso { MANTIDA, DIMINUIDA, ANULADA }

    /**
     * A decisão do recurso (art. 84.º n.º 4): manter, diminuir ou anular a pena. Mantida ou diminuída, a pena executa-se a
     * partir do dia seguinte; anulada, o processo termina sem pena.
     */
    public void decidirRecurso(LocalDate data, ResultadoRecurso resultado, PenaDisciplinar novaPena, Integer duracao) {
        exigir(FaseProcessoDisciplinar.EM_RECURSO, "decidir o recurso");
        if (resultado == null) throw invalido("Diga se a pena é mantida, diminuída ou anulada.");
        data = naoAntes(data, ActoDisciplinar.Tipo.RECURSO, "o recurso");
        if (resultado == ResultadoRecurso.DIMINUIDA) {
            if (novaPena == null) throw invalido("Indique a pena que fica.");
            validarPena(novaPena, duracao);
            boolean menor = !novaPena.pelomenos(pena) || (novaPena == pena && duracao != null && penaDuracao != null && duracao < penaDuracao);
            if (!menor) throw invalido("Diminuir é aplicar pena menos grave, ou mais curta.");
            this.pena = novaPena;
            this.penaDuracao = duracao;
            this.penalty = rotulo(novaPena, duracao);
        }
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.DECISAO_RECURSO, data, null, null,
                resultado == ResultadoRecurso.ANULADA ? null : pena, penaDuracao, resultado.name()));
        if (resultado == ResultadoRecurso.ANULADA) {
            this.pena = null;
            this.penaDuracao = null;
            this.penalty = "Anulada em recurso";
            this.penaltyStartDate = null;
            this.penaltyEndDate = null;
            this.fase = FaseProcessoDisciplinar.CONCLUIDO;
            return;
        }
        // Já executada (uma pena leve executa-se no dia seguinte à notificação e o recurso pode vir depois): o período
        // mantém-se, ajustado à nova duração; por executar, executa-se a partir do dia seguinte à decisão do recurso.
        if (efeitosAplicadosEm != null) {
            if (pena.temPeriodo() && penaltyStartDate != null) this.penaltyEndDate = pena.fim(penaltyStartDate, penaDuracao);
            this.fase = FaseProcessoDisciplinar.CONCLUIDO;
            return;
        }
        definirPeriodo(data.plusDays(1));
        this.fase = FaseProcessoDisciplinar.NOTIFICADO;
    }

    // ---------------------------------------------------------------- efeitos

    /**
     * Quando se executa a pena: no dia seguinte à notificação (art. 77.º) ou à decisão do recurso; as que acabam com o
     * exercício de funções só depois de passar o prazo de recurso, porque o recurso suspende a execução e elas não se
     * desfazem [interp.]. Nulo se não há o que executar.
     */
    public LocalDate dataExecucao() {
        if (pena == null || efeitosAplicadosEm != null) return null;
        if (suspensaAnos() != null)
            return ultimo(ActoDisciplinar.Tipo.CADUCIDADE_SUSPENSAO).map(c -> c.data().plusDays(1)).orElse(null);
        if (fase != FaseProcessoDisciplinar.NOTIFICADO) return null;
        var recurso = ultimo(ActoDisciplinar.Tipo.DECISAO_RECURSO);
        if (recurso.isPresent()) return recurso.get().data().plusDays(1);
        var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).orElseThrow();
        return pena.expulsiva() ? n.dataFim().plusDays(1) : n.data().plusDays(1);
    }

    public boolean efeitosDevidos(LocalDate hoje) {
        var d = dataExecucao();
        return d != null && !hoje.isBefore(d);
    }

    /** Os efeitos aplicados; o processo conclui-se quando já não cabe recurso (ou este foi decidido). */
    public void marcarEfeitosAplicados(LocalDate hoje, LocalDateTime agora, String texto) {
        this.efeitosAplicadosEm = agora;
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.EFEITOS, hoje, texto));
        concluirSeTransitado(hoje);
    }

    /** Executada a pena e passado o prazo de recurso sem recurso (ou decidido este), o processo está concluído. */
    public boolean concluirSeTransitado(LocalDate hoje) {
        if (fase != FaseProcessoDisciplinar.NOTIFICADO || (efeitosAplicadosEm == null && suspensaAnos() == null)) return false;
        boolean recursoDecidido = ultimo(ActoDisciplinar.Tipo.DECISAO_RECURSO).isPresent();
        var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).orElseThrow();
        if (!recursoDecidido && !hoje.isAfter(n.dataFim())) return false;
        this.fase = FaseProcessoDisciplinar.CONCLUIDO;
        return true;
    }

    // ---------------------------------------------------------------- suspensão da pena, reabilitação, revisão

    /** Os anos por que a pena ficou suspensa (art. 34.º), ou nulo. */
    public Integer suspensaAnos() {
        if (pena == null) return null;
        return ultimo(ActoDisciplinar.Tipo.DECISAO).map(ActoDisciplinar::dias).orElse(null);
    }

    /** O último dia da suspensão da pena: os anos contam da notificação (art. 34.º n.º 2). */
    public LocalDate suspensaAte() {
        Integer anos = suspensaAnos();
        if (anos == null) return null;
        return ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).map(n -> n.data().plusYears(anos)).orElse(null);
    }

    /** A pena está suspensa neste dia (e não caducou). */
    public boolean penaSuspensaEm(LocalDate dia) {
        var ate = suspensaAte();
        return ate != null && !dia.isAfter(ate) && ultimo(ActoDisciplinar.Tipo.CADUCIDADE_SUSPENSAO).isEmpty();
    }

    /** A suspensão caduca se o agente é punido de novo durante ela (art. 34.º n.º 4): a pena executa-se no dia seguinte. */
    public void caducarSuspensao(LocalDate data, String motivo) {
        if (!penaSuspensaEm(data)) throw IgrpResponseStatusException.conflict("A pena não está suspensa nessa data.");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.CADUCIDADE_SUSPENSAO, data, motivo));
        definirPeriodo(data.plusDays(1));
    }

    /**
     * A reabilitação (art. 95.º): só das penas de aposentação compulsiva e demissão, executadas, decorridos 5 anos sobre a
     * aplicação; faz cessar as incapacidades, mas não devolve o lugar (n.º 5).
     */
    public void reabilitar(LocalDate data, String despacho) {
        if (pena != PenaDisciplinar.APOSENTACAO_COMPULSIVA && pena != PenaDisciplinar.DEMISSAO)
            throw invalido("A reabilitação é das penas de aposentação compulsiva e de demissão (art. 95.º n.º 1).");
        var efeitos = ultimo(ActoDisciplinar.Tipo.EFEITOS)
                .orElseThrow(() -> IgrpResponseStatusException.conflict("A pena ainda não foi executada."));
        if (ultimo(ActoDisciplinar.Tipo.REABILITACAO).isPresent()) throw IgrpResponseStatusException.conflict("Já foi reabilitado.");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho que concede a reabilitação.");
        if (data == null || data.isBefore(efeitos.data().plusYears(5)))
            throw invalido("A reabilitação só se pede passados 5 anos sobre a aplicação da pena (a partir de "
                    + Datas.pt(efeitos.data().plusYears(5)) + ") — art. 95.º n.º 3.");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.REABILITACAO, data, despacho.trim()));
    }

    public enum ResultadoRevisao { REVOGADA, ALTERADA }

    /**
     * A revisão procedente (arts. 90.º–94.º), a todo o tempo: revoga ou altera a pena, nunca a agrava (art. 90.º n.º 3). A
     * revogação cancela o registo da pena e anula os seus efeitos (art. 94.º n.º 2).
     */
    public void rever(LocalDate data, ResultadoRevisao resultado, PenaDisciplinar novaPena, Integer duracao, String despacho) {
        if (pena == null || (fase != FaseProcessoDisciplinar.CONCLUIDO && fase != FaseProcessoDisciplinar.NOTIFICADO))
            throw IgrpResponseStatusException.conflict("Só se revê um processo decidido com pena.");
        if (resultado == null) throw invalido("Diga se a pena é revogada ou alterada.");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho da revisão.");
        if (data == null) throw invalido("Indique a data do acto.");
        if (resultado == ResultadoRevisao.ALTERADA) {
            if (novaPena == null) throw invalido("Indique a pena que fica.");
            validarPena(novaPena, duracao);
            boolean agrava = novaPena != pena ? novaPena.pelomenos(pena) : duracao != null && penaDuracao != null && duracao > penaDuracao;
            if (agrava) throw invalido("A revisão não pode agravar a pena (art. 90.º n.º 3).");
            this.pena = novaPena;
            this.penaDuracao = duracao;
            this.penalty = rotulo(novaPena, duracao) + " (alterada em revisão)";
            if (novaPena.temPeriodo() && penaltyStartDate != null) this.penaltyEndDate = novaPena.fim(penaltyStartDate, duracao);
        } else {
            this.penalty = "Revogada em revisão";
            this.pena = null;
            this.penaDuracao = null;
            this.penaltyStartDate = null;
            this.penaltyEndDate = null;
        }
        actos.add(new ActoDisciplinar(java.util.UUID.randomUUID(), ActoDisciplinar.Tipo.REVISAO, data, null, null, pena, penaDuracao,
                resultado.name() + ": " + despacho.trim()));
        this.fase = FaseProcessoDisciplinar.CONCLUIDO;
    }

    /** O arquivamento antes da decisão: pelo despacho liminar (art. 50.º n.º 2) ou por outro motivo (desistência, morte…). */
    public void arquivar(LocalDate data, String motivo) {
        if (fase == null || !fase.antesDaDecisao())
            throw IgrpResponseStatusException.conflict("Só se arquiva um processo em curso, antes da decisão.");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique o motivo do arquivamento.");
        actos.add(ActoDisciplinar.de(ActoDisciplinar.Tipo.ARQUIVAMENTO, data, motivo.trim()));
        this.endDate = data;
        this.fase = FaseProcessoDisciplinar.ARQUIVADO;
    }

    // ---------------------------------------------------------------- leitura

    /** O colaborador é arguido neste processo (depois de instaurado e antes da decisão). */
    public boolean arguidoEmCurso() {
        return fase != null && fase != FaseProcessoDisciplinar.PARTICIPADO && fase.antesDaDecisao() && !especie.semArguido();
    }

    /** Os dias em que o colaborador está afastado: a suspensão preventiva e a pena de suspensão/inactividade em execução. */
    public List<LocalDate[]> periodosDeAfastamento() {
        var l = new ArrayList<LocalDate[]>();
        suspensoes().forEach(a -> l.add(new LocalDate[]{a.data(), a.dataFim()}));
        if (pena != null && pena.temPeriodo() && efeitosAplicadosEm != null && penaltyStartDate != null)
            l.add(new LocalDate[]{penaltyStartDate, penaltyEndDate});
        return l;
    }

    /** Os prazos que correm na fase em que o processo está (BR-DIS-05). {@code util} diz se um dia é útil. */
    public List<Prazo> prazos(LocalDate hoje, Predicate<LocalDate> util) {
        var l = new ArrayList<Prazo>();
        if (fase == null) return l;
        switch (fase) {
            case PARTICIPADO -> {
                if (dataInfraccao != null && penaPrevista != null)
                    l.add(prazo("Prescrição do procedimento (instaure antes)", penaPrevista.prescricao(dataInfraccao), hoje));
            }
            case INSTAURADO -> ultimo(ActoDisciplinar.Tipo.NOMEACAO_INSTRUTOR)
                    .ifPresent(n -> l.add(prazo("Início da instrução", diasUteis(n.data(), 3, util), hoje)));
            case EM_INSTRUCAO -> {
                LocalDate fim = fimDaInstrucao();
                l.add(prazo("Fim da instrução", fim, hoje));
                l.add(prazo("Acusação ou relatório", diasUteis(fim, 5, util), hoje));
            }
            case ACUSADO -> {
                var n = ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_ACUSACAO);
                if (n.isEmpty()) {
                    l.add(prazo("Notificação da acusação", ultimo(ActoDisciplinar.Tipo.ACUSACAO).orElseThrow().data().plusDays(2), hoje));
                } else {
                    var defesa = ultimo(ActoDisciplinar.Tipo.DEFESA);
                    if (defesa.isEmpty()) l.add(prazo("Defesa do arguido", n.get().dataFim(), hoje));
                    LocalDate base = defesa.map(ActoDisciplinar::data).orElse(n.get().dataFim());
                    l.add(prazo("Relatório final", base.plusDays(10), hoje));
                }
            }
            case RELATORIO -> l.add(prazo("Decisão", diasUteis(ultimo(ActoDisciplinar.Tipo.RELATORIO).orElseThrow().data(), 15, util), hoje));
            case NOTIFICADO -> {
                if (ultimo(ActoDisciplinar.Tipo.DECISAO_RECURSO).isEmpty())
                    ultimo(ActoDisciplinar.Tipo.NOTIFICACAO_DECISAO).ifPresent(n -> l.add(prazo("Recurso hierárquico", n.dataFim(), hoje)));
                var ex = dataExecucao();
                if (ex != null) l.add(prazo("Execução da pena", ex, hoje));
            }
            default -> { }
        }
        suspensoes().stream().filter(s -> !hoje.isAfter(s.dataFim()))
                .forEach(s -> l.add(prazo("Fim da suspensão preventiva", s.dataFim(), hoje)));
        return l;
    }

    public LocalDate fimDaInstrucao() {
        var inicio = ultimo(ActoDisciplinar.Tipo.INICIO_INSTRUCAO).map(ActoDisciplinar::data).orElse(null);
        if (inicio == null) return null;
        int extra = ultimo(ActoDisciplinar.Tipo.PRORROGACAO_INSTRUCAO).map(ActoDisciplinar::dias).orElse(0);
        return inicio.plusDays(DIAS_INSTRUCAO + extra);
    }

    /** A data da prescrição do procedimento pela pena prevista, se se sabe. */
    public LocalDate prescreveEm() {
        return dataInfraccao != null && penaPrevista != null ? penaPrevista.prescricao(dataInfraccao) : null;
    }

    public Optional<ActoDisciplinar> ultimo(ActoDisciplinar.Tipo tipo) {
        return actos.stream().filter(a -> a.tipo() == tipo).reduce((a, b) -> b);
    }

    private void definirPeriodo(LocalDate inicio) {
        if (pena != null && pena.temPeriodo()) {
            this.penaltyStartDate = inicio;
            this.penaltyEndDate = pena.fim(inicio, penaDuracao);
        } else {
            this.penaltyStartDate = pena != null ? inicio : null;
            this.penaltyEndDate = null;
        }
    }

    private static void validarPena(PenaDisciplinar p, Integer duracao) {
        String erro = p.validarDuracao(duracao);
        if (erro != null) throw invalido(erro);
    }

    static String rotulo(PenaDisciplinar p, Integer duracao) {
        String n = p.nome();
        String base = Character.toUpperCase(n.charAt(0)) + n.substring(1);
        return switch (p) {
            case MULTA -> base + " (" + duracao + " dias de remuneração)";
            case SUSPENSAO -> base + " (" + duracao + " dias)";
            case INACTIVIDADE -> base + " (" + duracao + " meses)";
            default -> base;
        };
    }

    private static LocalDate diasUteis(LocalDate de, int n, Predicate<LocalDate> util) {
        LocalDate d = de;
        int contados = 0;
        while (contados < n) {
            d = d.plusDays(1);
            if (util.test(d)) contados++;
        }
        return d;
    }

    private static Prazo prazo(String nome, LocalDate data, LocalDate hoje) {
        return new Prazo(nome, data, data != null && hoje.isAfter(data));
    }

    private LocalDate naoAntes(LocalDate data, ActoDisciplinar.Tipo anterior, String oQue) {
        if (data == null) throw invalido("Indique a data do acto.");
        var a = ultimo(anterior);
        if (a.isPresent() && data.isBefore(a.get().data()))
            throw invalido("A data não pode ser antes de " + oQue + " (" + Datas.pt(a.get().data()) + ").");
        return data;
    }

    private void exigir(FaseProcessoDisciplinar esperada, String accao) {
        if (fase == null) throw IgrpResponseStatusException.conflict("Este processo é só um registo, sem tramitação.");
        if (fase != esperada) throw IgrpResponseStatusException.conflict("Não é possível " + accao + " na fase em que o processo está.");
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
