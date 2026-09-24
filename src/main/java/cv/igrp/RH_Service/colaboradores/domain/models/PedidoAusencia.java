package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class PedidoAusencia {

    private PedidoAusenciaId id;
    private FuncionarioId funcionarioId;
    private TipoAusenciaId tipoAusenciaId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int numeroDias;
    private String motivo;
    private EstadoPedidoAusencia estado;
    private FuncionarioId aprovadoPor;
    private LocalDate dataDecisao;
    private String observacoesDecisao;
    private Boolean isActive;
    /** Data a partir da qual as férias deixaram de correr (art. 8.º n.º 3). Ver V51. */
    private LocalDate suspensoEm;
    /** Qual das causas do art. 8.º. Texto da instituição, não validado — mas obrigatório. */
    private String suspensaoMotivo;
    /**
     * A opção do art. 43.º n.º 2, quando o tipo é falta injustificada: perder a remuneração dos
     * dias, ou descontá-los nas férias. É a <b>única</b> escolha que esse número dá — o desconto
     * na antiguidade, no mesmo número, é imperativo — e é de cada caso, por isso vive aqui e não
     * no catálogo.
     */
    private OpcaoFaltaInjustificada opcaoFaltaInjustificada;
    /**
     * Pedido em horas (V58): as horas valem em cada dia do intervalo — um dia para o tratamento
     * ambulatório, meses para a amamentação. As duas ou nenhuma; sem elas, é de dias inteiros.
     */
    private LocalTime horaInicio;
    private LocalTime horaFim;

    private PedidoAusencia() {}

    public static PedidoAusencia criar(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId,
                                       LocalDate dataInicio, LocalDate dataFim,
                                       int numeroDias, String motivo,
                                       OpcaoFaltaInjustificada opcaoFaltaInjustificada) {
        PedidoAusencia p = new PedidoAusencia();
        p.id = PedidoAusenciaId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.tipoAusenciaId = tipoAusenciaId;
        p.dataInicio = dataInicio;
        p.dataFim = dataFim;
        p.numeroDias = numeroDias;
        p.motivo = motivo;
        p.estado = EstadoPedidoAusencia.PENDENTE;
        p.isActive = true;
        p.opcaoFaltaInjustificada = opcaoFaltaInjustificada;
        return p;
    }

    public static PedidoAusencia reconstituir(PedidoAusenciaId id, FuncionarioId funcionarioId,
                                              TipoAusenciaId tipoAusenciaId, LocalDate dataInicio,
                                              LocalDate dataFim, int numeroDias, String motivo,
                                              String estado, FuncionarioId aprovadoPor,
                                              LocalDate dataDecisao, String observacoesDecisao,
                                              Boolean isActive, LocalDate suspensoEm,
                                              String suspensaoMotivo,
                                              OpcaoFaltaInjustificada opcaoFaltaInjustificada) {
        PedidoAusencia p = new PedidoAusencia();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.tipoAusenciaId = tipoAusenciaId;
        p.dataInicio = dataInicio;
        p.dataFim = dataFim;
        p.numeroDias = numeroDias;
        p.motivo = motivo;
        p.estado = EstadoPedidoAusencia.de(estado);
        p.aprovadoPor = aprovadoPor;
        p.dataDecisao = dataDecisao;
        p.observacoesDecisao = observacoesDecisao;
        p.isActive = isActive;
        p.suspensoEm = suspensoEm;
        p.suspensaoMotivo = suspensaoMotivo;
        p.opcaoFaltaInjustificada = opcaoFaltaInjustificada;
        return p;
    }

    /** Chamado na criação e na reconstituição. As duas horas ou nenhuma, e o início antes do fim. */
    public void definirHoras(LocalTime inicio, LocalTime fim) {
        if (inicio == null && fim == null) return;
        if (inicio == null || fim == null)
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Um pedido em horas tem horaInicio e horaFim.");
        if (!inicio.isBefore(fim))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A horaInicio (" + inicio + ") tem de ser antes da horaFim (" + fim + ").");
        this.horaInicio = inicio;
        this.horaFim = fim;
    }

    public boolean isEmHoras() { return horaInicio != null; }

    /** Minutos por dia de um pedido em horas; zero num de dias inteiros. */
    public int minutosPorDia() {
        return isEmHoras() ? (int) Duration.between(horaInicio, horaFim).toMinutes() : 0;
    }

    /**
     * <b>Terminar antes do fim</b> um pedido em horas aprovado — a amamentação que acaba mais cedo.
     * Como a suspensão das férias: a decisão fica, o período acaba na véspera da data indicada.
     */
    public void terminar(LocalDate aPartirDe, String motivo) {
        if (!isEmHoras())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só se termina antes do fim um pedido em horas; as férias suspendem-se, os outros cancelam-se.");
        if (!EstadoPedidoAusencia.APROVADO.equals(this.estado))
            throw IgrpResponseStatusException.conflict("Só um pedido aprovado pode ser terminado. Estado actual: " + this.estado);
        if (this.suspensoEm != null)
            throw IgrpResponseStatusException.conflict("Este pedido já terminou a " + this.suspensoEm + ".");
        if (aPartirDe == null || motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "Terminar exige a data a partir da qual deixa de valer e o motivo.");
        if (!aPartirDe.isAfter(this.dataInicio) || aPartirDe.isAfter(this.dataFim))
            throw IgrpResponseStatusException.of(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data tem de ser depois do início (" + this.dataInicio + ") e até ao fim (" + this.dataFim
                            + "). Antes de começar, cancela-se.");
        this.suspensoEm = aPartirDe;
        this.suspensaoMotivo = motivo.trim();
    }

    /** O último dia em que o pedido vale: a véspera da suspensão ou do fim antecipado, ou o fim. */
    public LocalDate ultimoDiaEmVigor() {
        return suspensoEm != null && suspensoEm.minusDays(1).isBefore(dataFim) ? suspensoEm.minusDays(1) : dataFim;
    }

    public void aprovar(FuncionarioId aprovadoPorId, LocalDate dataDecisao, String observacoes) {
        if (!EstadoPedidoAusencia.PENDENTE.equals(this.estado))
            throw IgrpResponseStatusException.conflict("Só é possível aprovar pedidos em estado PENDENTE. Estado actual: " + this.estado);
        this.estado = EstadoPedidoAusencia.APROVADO;
        this.aprovadoPor = aprovadoPorId;
        this.dataDecisao = dataDecisao;
        this.observacoesDecisao = observacoes;
    }

    /**
     * <b>Aprovação automática</b> — o tipo não requer aprovação ({@code requires_approval = false} no
     * catálogo): são as faltas que a lei dá como direito (casamento, luto, doença, nascimento, greve —
     * DL n.º 3/2010, art. 15.º), em que basta comunicar e provar. Fica sem decisor; se a prova faltar,
     * o RH cancela ou regista falta injustificada.
     */
    public void aprovarAutomaticamente(LocalDate hoje) {
        aprovar(null, hoje, "Aprovado automaticamente: o tipo não requer aprovação (basta comunicar e provar).");
    }

    /** Aprovado sem decisor: foi o sistema, porque o tipo não requer aprovação. */
    public boolean isAprovacaoAutomatica() {
        return EstadoPedidoAusencia.APROVADO.equals(estado) && aprovadoPor == null && dataDecisao != null;
    }

    public void rejeitar(FuncionarioId aprovadoPorId, LocalDate dataDecisao, String observacoes) {
        if (!EstadoPedidoAusencia.PENDENTE.equals(this.estado))
            throw IgrpResponseStatusException.conflict("Só é possível rejeitar pedidos em estado PENDENTE. Estado actual: " + this.estado);
        this.estado = EstadoPedidoAusencia.REJEITADO;
        this.aprovadoPor = aprovadoPorId;
        this.dataDecisao = dataDecisao;
        this.observacoesDecisao = observacoes;
    }

    public void cancelar() {
        cancelar(null, null, null);
    }

    /**
     * Retira o pedido. Quem cancela fica registado — pode ser o próprio ou o RH,
     * e é útil saber qual foi.
     */
    public void cancelar(FuncionarioId canceladoPor, LocalDate data, String observacoes) {
        if (this.estado != null && this.estado.isSemEfeito())
            throw IgrpResponseStatusException.conflict("Não é possível cancelar um pedido com estado: " + this.estado);
        this.estado = EstadoPedidoAusencia.CANCELADO;
        if (canceladoPor != null) this.aprovadoPor = canceladoPor;
        if (data != null) this.dataDecisao = data;
        if (observacoes != null) this.observacoesDecisao = observacoes;
    }

    /**
     * <b>Suspender as férias</b> — art. 8.º do DL n.º 3/2010. A suspensão produz efeito
     * <b>a partir</b> da data indicada (n.º 3: «a partir da data da entrada no serviço do
     * documento comprovativo»), logo o último dia de férias é a <b>véspera</b> — a mesma leitura
     * que o regresso antecipado da licença (art. 46.º n.º 4).
     *
     * <p><b>O estado não muda.</b> O pedido continua {@code APROVADO}: a decisão foi tomada e não
     * se desfaz; o que encurta é o período. É a lição da V48 — não voltar a guardar o período
     * dentro do campo que guarda a decisão.
     *
     * <p>Não se suspende o que ainda não começou (aí cancela-se) nem o que já terminou (não há
     * nada a interromper), e suspender no próprio dia de início deixaria o fim antes do começo.
     *
     * @return o número de dias de calendário que deixaram de ser férias — quem os converte em
     *         dias úteis e os devolve ao saldo é o serviço, que tem o calendário de feriados
     */
    public void suspender(LocalDate dataSuspensao, String motivo, LocalDate hoje) {
        if (!EstadoPedidoAusencia.APROVADO.equals(this.estado))
            throw IgrpResponseStatusException.conflict(
                    "Só um pedido aprovado pode ser suspenso. Estado actual: " + this.estado);
        if (this.suspensoEm != null)
            throw IgrpResponseStatusException.conflict(
                    "Este pedido já foi suspenso a " + this.suspensoEm + ".");
        if (dataSuspensao == null)
            throw IgrpResponseStatusException.badRequest("A data da suspensão é obrigatória.");
        if (motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.badRequest(
                    "A suspensão de férias exige um motivo: a lei só a admite pelas causas do "
                            + "art. 8.º (parentalidade, doença, assistência a familiares, ou razões "
                            + "imperiosas de serviço).");
        if (dataSuspensao.isAfter(hoje))
            throw IgrpResponseStatusException.badRequest(
                    "A data da suspensão (" + dataSuspensao + ") não pode ser futura.");
        if (!dataSuspensao.isAfter(this.dataInicio))
            throw IgrpResponseStatusException.conflict(
                    "As férias começam a " + this.dataInicio + " e a suspensão só produz efeito a "
                            + "partir da data indicada: não há período nenhum gozado a interromper. "
                            + "Para desfazer o pedido, cancele-o.");
        if (this.dataFim != null && dataSuspensao.isAfter(this.dataFim))
            throw IgrpResponseStatusException.conflict(
                    "Estas férias terminaram a " + this.dataFim + ": não há nada a suspender.");

        this.suspensoEm = dataSuspensao;
        this.suspensaoMotivo = motivo;
        this.dataFim = dataSuspensao.minusDays(1);
    }

    /** Foi interrompido a meio (art. 8.º)? */
    public boolean isSuspenso() { return suspensoEm != null; }

    /** Actualiza a contagem depois de o período encurtar. Quem a calcula é o serviço. */
    public void ajustarNumeroDias(int dias) {
        this.numeroDias = Math.max(0, dias);
    }

    /** Nome do estado, para persistência e respostas. */
    public String getEstadoTexto() {
        return EstadoPedidoAusencia.texto(this.estado);
    }
}
