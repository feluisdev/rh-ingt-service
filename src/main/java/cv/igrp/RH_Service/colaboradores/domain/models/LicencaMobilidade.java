package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Licença ou mobilidade — processo com despacho e aprovação.
 *
 * <p><b>A mobilidade transitória não tira o Lugar ao titular</b> (Lei n.º 20/X/2023, art. 135.º
 * n.º 7: "sem ocupação do lugar do quadro"; art. 137.º: o tempo conta no lugar de origem). Este
 * registo diz apenas <i>onde a pessoa exerce funções</i> e até quando; a titularidade do Lugar
 * vive na Afectação e não é tocada. A mudança <b>definitiva</b> de Lugar é outra coisa: é a
 * transferência.
 *
 * <p>O destino é interno (uma unidade nossa, {@code destinationUnitId}) ou externo (uma entidade
 * de fora, {@code entidadeDestino}) — o comportamento é o mesmo nos dois casos.
 */
@Getter
public class LicencaMobilidade {

    // O status guarda a DECISÃO, e só a decisão (art. 44.º n.º 2: o despacho). O estado do
    // PERÍODO — por iniciar, em curso, terminada — deriva das datas e vive no
    // EstadoPeriodoLicenca. Até à V48 os dois viviam aqui, e era isso que tornava possível
    // aprovar em Setembro uma licença de Outubro e pôr a pessoa de licença em Setembro.
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";

    private LicencaMobilidadeId id;
    private FuncionarioId funcionarioId;
    private SubtipoLicencaMobilidadeId subtipoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private Boolean isActive;
    private String status;
    /** Destino interno: unidade orgânica nossa onde passa a exercer funções. */
    private UUID destinationUnitId;
    /**
     * Legado do modelo anterior, em que a mobilidade abria uma afectação no destino. A mobilidade
     * transitória não ocupa Lugar no destino; a mudança de Lugar faz-se pela transferência.
     */
    private UUID destinationPositionId;
    private String justification;
    private UUID documentId;
    private String rejectionReason;
    /** Prorrogações já concedidas (art. 132.º n.º 5: em regra, uma única). */
    private Integer extensionsCount;
    /**
     * Quando os efeitos da entrada em vigor foram aplicados ao Lugar. Nulo quer dizer «ainda por
     * aplicar», e é isso — e só isso — que o job diário lê. É o que o torna seguro de repetir:
     * um dia falhado não perde nada, porque a pergunta é sobre o estado actual e não sobre um
     * intervalo desde a última execução.
     */
    private LocalDateTime efeitoEntradaAplicadoEm;
    /** O mesmo para o regresso, no fim do período. */
    private LocalDateTime efeitoRegressoAplicadoEm;
    /**
     * Como a mobilidade é prestada (art. 134.º n.º 2): a tempo inteiro ou em acumulação com as
     * funções do serviço de origem. Numa licença não tem sentido — quem está de licença não
     * exerce funções em lado nenhum — e por isso lá vale sempre {@code TEMPO_INTEIRO}.
     */
    private FormaPrestacaoMobilidade formaPrestacao;

    private LicencaMobilidade() {}

    public static LicencaMobilidade criar(FuncionarioId funcionarioId,
                                          SubtipoLicencaMobilidadeId subtipoId,
                                          LocalDate dataInicio, LocalDate dataFim,
                                          String entidadeDestino, String despachoNumero,
                                          String observacoes, String justification,
                                          UUID destinationUnitId, UUID destinationPositionId,
                                          UUID documentId) {
        if (dataInicio == null)
            throw IgrpResponseStatusException.badRequest("A data de início é obrigatória.");
        if (dataFim != null && dataFim.isBefore(dataInicio))
            throw IgrpResponseStatusException.badRequest("A data de fim não pode ser anterior à data de início.");

        LicencaMobilidade l = new LicencaMobilidade();
        l.id = LicencaMobilidadeId.gerarNovo();
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.justification = justification;
        l.destinationUnitId = destinationUnitId;
        l.destinationPositionId = destinationPositionId;
        l.documentId = documentId;
        l.isActive = true;
        l.status = PENDING;
        l.extensionsCount = 0;
        l.formaPrestacao = FormaPrestacaoMobilidade.TEMPO_INTEIRO;
        return l;
    }

    public static LicencaMobilidade reconstituir(LicencaMobilidadeId id,
                                                  FuncionarioId funcionarioId,
                                                  SubtipoLicencaMobilidadeId subtipoId,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String entidadeDestino, String despachoNumero,
                                                  String observacoes, Boolean isActive,
                                                  String status, UUID destinationUnitId,
                                                  UUID destinationPositionId,
                                                  String justification, UUID documentId,
                                                  String rejectionReason, Integer extensionsCount,
                                                  LocalDateTime efeitoEntradaAplicadoEm,
                                                  LocalDateTime efeitoRegressoAplicadoEm) {
        LicencaMobilidade l = new LicencaMobilidade();
        l.id = id;
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.isActive = isActive;
        l.status = status != null ? status : PENDING;
        l.destinationUnitId = destinationUnitId;
        l.destinationPositionId = destinationPositionId;
        l.justification = justification;
        l.documentId = documentId;
        l.rejectionReason = rejectionReason;
        l.extensionsCount = extensionsCount != null ? extensionsCount : 0;
        l.efeitoEntradaAplicadoEm = efeitoEntradaAplicadoEm;
        l.efeitoRegressoAplicadoEm = efeitoRegressoAplicadoEm;
        l.formaPrestacao = FormaPrestacaoMobilidade.TEMPO_INTEIRO;
        return l;
    }

    /**
     * Reconstituição completa, com a forma de prestação (art. 134.º n.º 2). É esta que o mapper
     * usa; a sobrecarga sem ela assume {@code TEMPO_INTEIRO}, que é a regra do art. 20.º.
     */
    public static LicencaMobilidade reconstituir(LicencaMobilidadeId id,
                                                  FuncionarioId funcionarioId,
                                                  SubtipoLicencaMobilidadeId subtipoId,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String entidadeDestino, String despachoNumero,
                                                  String observacoes, Boolean isActive,
                                                  String status, UUID destinationUnitId,
                                                  UUID destinationPositionId,
                                                  String justification, UUID documentId,
                                                  String rejectionReason, Integer extensionsCount,
                                                  LocalDateTime efeitoEntradaAplicadoEm,
                                                  LocalDateTime efeitoRegressoAplicadoEm,
                                                  FormaPrestacaoMobilidade formaPrestacao) {
        LicencaMobilidade l = reconstituir(id, funcionarioId, subtipoId, dataInicio, dataFim,
                entidadeDestino, despachoNumero, observacoes, isActive, status, destinationUnitId,
                destinationPositionId, justification, documentId, rejectionReason, extensionsCount,
                efeitoEntradaAplicadoEm, efeitoRegressoAplicadoEm);
        l.formaPrestacao = formaPrestacao != null ? formaPrestacao : FormaPrestacaoMobilidade.TEMPO_INTEIRO;
        return l;
    }

    /**
     * Fixa a forma de prestação (art. 134.º n.º 2). Só enquanto o processo estiver por decidir:
     * depois do despacho, mudar de exclusividade para acumulação é outro despacho, não uma
     * correcção. Nulo vale {@code TEMPO_INTEIRO}, que é a regra do art. 20.º.
     */
    public void definirFormaPrestacao(FormaPrestacaoMobilidade forma) {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "A forma de prestação só se define enquanto o processo está por decidir. "
                            + "Estado actual: " + this.status);
        this.formaPrestacao = forma != null ? forma : FormaPrestacaoMobilidade.TEMPO_INTEIRO;
    }

    public void atualizar(LocalDate dataInicio, LocalDate dataFim, String entidadeDestino,
                          String despachoNumero, String observacoes) {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Só um registo PENDING pode ser actualizado. Estado actual: " + this.status);
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.entidadeDestino = entidadeDestino;
        this.despachoNumero = despachoNumero;
        this.observacoes = observacoes;
    }

    /**
     * Deferir o pedido (art. 44.º n.º 2). Aprovar <b>não</b> é pôr em vigor: a licença entra em
     * vigor na sua data de início, que pode ser hoje ou daqui a um mês. Quem aplica os efeitos
     * no Lugar é o {@code LicencaService}, na data certa.
     */
    public void aprovar() {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser aprovados. Estado actual: " + this.status);
        this.isActive = true;
        this.status = APPROVED;
    }

    public void rejeitar(String reason) {
        if (!isPending())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING podem ser rejeitados. Estado actual: " + this.status);
        this.isActive = false;
        this.status = REJECTED;
        this.rejectionReason = reason;
    }

    /**
     * <b>Regresso antecipado ao serviço</b> (art. 46.º n.º 4): o funcionário volta antes do fim
     * despachado, e o que isso muda é a <b>data de fim</b> — não o despacho, que continua a ser o
     * que foi. Por isso não há transição de estado nenhuma aqui: o registo fica {@code APPROVED}
     * com um período mais curto, e passa a {@link EstadoPeriodoLicenca#TERMINADA} sozinho.
     *
     * <p>Só quem partiu pode regressar. Uma licença <b>por iniciar</b> não se encerra: desiste-se
     * dela ({@link #cancelar()}), porque pelo art. 44.º n.º 1 não chegou a haver ausência. Uma já
     * terminada não precisa de nada. Era daqui que vinha o fim anterior ao início.
     */
    public void registarRegressoAntecipado(LocalDate dataRegresso, LocalDate hoje) {
        if (!isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Só uma licença/mobilidade deferida (APPROVED) admite regresso antecipado. "
                            + "Estado actual: " + this.status);

        EstadoPeriodoLicenca periodo = estadoEm(hoje);
        if (periodo == EstadoPeriodoLicenca.POR_INICIAR)
            throw IgrpResponseStatusException.conflict(
                    "Esta licença/mobilidade ainda não começou (início a " + this.dataInicio
                            + "): não há regresso a registar. Para desistir dela, use o cancelamento.");
        if (periodo == EstadoPeriodoLicenca.TERMINADA)
            throw IgrpResponseStatusException.conflict(
                    "Esta licença/mobilidade já terminou a " + this.dataFim + ".");

        LocalDate efectiva = dataRegresso != null ? dataRegresso : hoje;
        if (efectiva.isBefore(this.dataInicio))
            throw IgrpResponseStatusException.badRequest(
                    "A data de regresso (" + efectiva + ") não pode ser anterior ao início da licença ("
                            + this.dataInicio + ").");
        if (efectiva.isAfter(hoje))
            throw IgrpResponseStatusException.badRequest(
                    "A data de regresso (" + efectiva + ") não pode ser futura.");

        // A data de regresso é o primeiro dia DE VOLTA ao serviço, logo o último dia de ausência
        // é a véspera. Fazer coincidir os dois punha a pessoa de licença no próprio dia em que
        // regressou, e um ecrã de RH mostrava-a ausente à frente de quem a via à secretária.
        //
        // Quem parte e regressa no mesmo dia esteve ausente parte desse dia: fica um dia, que é o
        // mínimo que este modelo sabe exprimir — `data_inicio`/`data_fim` são DATE, e o meio dia
        // do art. 13.º n.º 4 do DL n.º 3/2010 não tem como ser representado aqui. Também é o que
        // impede o fim de cair antes do início.
        LocalDate ultimoDiaDeAusencia = efectiva.minusDays(1);
        this.dataFim = ultimoDiaDeAusencia.isBefore(this.dataInicio) ? this.dataInicio : ultimoDiaDeAusencia;
    }

    /**
     * Desistir. Vale para um pedido ainda por decidir e para uma licença deferida que
     * <b>ainda não começou</b> — aí o despacho revoga-se e não fica ausência nenhuma. Depois de
     * começar já há ausência gozada, e a saída é o regresso antecipado (art. 46.º n.º 4).
     */
    public void cancelar(LocalDate hoje) {
        if (isApproved() && estadoEm(hoje) != EstadoPeriodoLicenca.POR_INICIAR)
            throw IgrpResponseStatusException.conflict(
                    "Esta licença/mobilidade já começou a " + this.dataInicio
                            + ": não pode ser cancelada. Registe o regresso antecipado.");
        if (!isPending() && !isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos PENDING ou APPROVED podem ser cancelados. Estado actual: " + this.status);
        this.isActive = false;
        this.status = CANCELLED;
    }

    /**
     * Prorroga o período. O limite de prorrogações vem do subtipo (parametrizado); a lei prevê,
     * em regra, uma única prorrogação por igual período (art. 132.º n.º 5).
     */
    public void prorrogar(LocalDate novaDataFim, Integer maxExtensions) {
        if (!isApproved())
            throw IgrpResponseStatusException.conflict(
                    "Apenas registos deferidos (APPROVED) podem ser prorrogados. Estado actual: " + this.status);
        if (novaDataFim == null)
            throw IgrpResponseStatusException.badRequest("A nova data de fim é obrigatória.");
        if (this.dataFim != null && !novaDataFim.isAfter(this.dataFim))
            throw IgrpResponseStatusException.badRequest(
                    "A nova data de fim tem de ser posterior à actual (" + this.dataFim + ").");
        if (maxExtensions != null && extensoes() >= maxExtensions)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Limite de prorrogações atingido (" + maxExtensions + ").");

        this.dataFim = novaDataFim;
        this.extensionsCount = extensoes() + 1;
    }

    /** Soft delete do registo (não é uma transição de estado do processo). */
    public void desativar() {
        this.isActive = false;
    }

    public void associarDocumento(UUID documentId) {
        this.documentId = documentId;
    }

    /** Duração em dias, contando o dia de início. Nulo quando o período é aberto. */
    public Long duracaoEmDias() {
        if (dataInicio == null || dataFim == null) return null;
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    public int extensoes() { return extensionsCount != null ? extensionsCount : 0; }

    /** Destino interno = uma unidade nossa; caso contrário é externo (entidade de fora). */
    public boolean isDestinoInterno() { return destinationUnitId != null; }

    public boolean isPending() { return PENDING.equals(this.status); }
    public boolean isApproved() { return APPROVED.equals(this.status); }

    // ---------------------------------------------------------------------------------
    // O período — o segundo eixo, derivado das datas e nunca guardado.
    // ---------------------------------------------------------------------------------

    /**
     * Onde o período está, na data dada. Só faz sentido num registo deferido: um pedido por
     * decidir, indeferido ou cancelado não tem período a decorrer, e devolve {@code null}.
     *
     * <p>Um período sem fim ({@code dataFim} nula) nunca termina sozinho — é o caso da licença de
     * longa duração, cujo regresso depende de despacho (art. 53.º) e não do calendário.
     */
    public EstadoPeriodoLicenca estadoEm(LocalDate data) {
        if (!isApproved()) return null;
        if (dataInicio != null && data.isBefore(dataInicio)) return EstadoPeriodoLicenca.POR_INICIAR;
        if (dataFim != null && data.isAfter(dataFim)) return EstadoPeriodoLicenca.TERMINADA;
        return EstadoPeriodoLicenca.EM_CURSO;
    }

    /** «Estar de licença» na data dada: deferida e a decorrer. */
    public boolean emVigorEm(LocalDate data) {
        return estadoEm(data) == EstadoPeriodoLicenca.EM_CURSO;
    }

    // ---------------------------------------------------------------------------------
    // Marcas de aplicação dos efeitos — a idempotência do job diário.
    // ---------------------------------------------------------------------------------

    /**
     * Os efeitos da entrada em vigor estão por aplicar nesta data? Verdadeiro quando a licença
     * está deferida, o início já chegou, e ninguém os aplicou ainda.
     */
    public boolean carecedeEfeitoEntrada(LocalDate data) {
        return isApproved() && efeitoEntradaAplicadoEm == null
                && estadoEm(data) != EstadoPeriodoLicenca.POR_INICIAR;
    }

    /**
     * Os efeitos do regresso estão por aplicar nesta data? Só depois de o período ter terminado —
     * é o «caduca automaticamente» do art. 46.º n.º 3, que não espera por ninguém.
     */
    public boolean carecedeEfeitoRegresso(LocalDate data) {
        return isApproved() && efeitoRegressoAplicadoEm == null
                && estadoEm(data) == EstadoPeriodoLicenca.TERMINADA;
    }

    public void marcarEfeitoEntradaAplicado(LocalDateTime quando) {
        this.efeitoEntradaAplicadoEm = quando != null ? quando : LocalDateTime.now();
    }

    public void marcarEfeitoRegressoAplicado(LocalDateTime quando) {
        this.efeitoRegressoAplicadoEm = quando != null ? quando : LocalDateTime.now();
    }
}
