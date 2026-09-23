package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * <b>Trabalho suplementar</b> (horas extras) — Lei n.º 20/X/2023, art. 155.º n.º 2 a): trabalho para
 * além do horário, em dia de descanso ou em feriado, que a Administração manda ou autoriza. Aqui só se
 * regista e se conta em horas; o suplemento (e o tecto de um terço da remuneração base, n.º 7) é do
 * processamento salarial.
 *
 * <p>É uma <b>autorização</b> para um intervalo de um dia. As horas <b>realizadas</b> não se declaram:
 * calculam-se das marcações válidas dentro do intervalo ({@link #minutosRealizados}). O que se fica a
 * mais num horário flexível sem autorização não é trabalho suplementar — é saldo da aferição.
 */
@Getter
public class TrabalhoSuplementar {

    private TrabalhoSuplementarId id;
    private FuncionarioId funcionarioId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    /** Porque é preciso. Obrigatório. */
    private String motivo;
    private EstadoTrabalhoSuplementar estado;
    /** Pedido pelo próprio ({@code /me}); falso quando a chefia ou o RH o lançaram. */
    private boolean pedidoPeloProprio;
    /** Autorizado depois do dia em que foi feito — o caso urgente. */
    private boolean autorizacaoPosterior;
    /** Quem autorizou ou recusou: a chefia directa (o seu id) ou nulo quando foi o RH. */
    private FuncionarioId decididoPor;
    private LocalDateTime decididoEm;
    private String motivoRecusa;
    private String motivoCancelamento;
    private LocalDateTime canceladoEm;

    private TrabalhoSuplementar() {}

    /**
     * Lançado pela chefia directa ({@code decisor}) ou pelo RH ({@code decisor} nulo): nasce autorizado.
     * Pode ser para um dia passado — é a autorização posterior, e fica assinalada.
     */
    public static TrabalhoSuplementar lancar(FuncionarioId funcionarioId, LocalDate data, LocalTime horaInicio,
                                             LocalTime horaFim, String motivo, FuncionarioId decisor,
                                             LocalDateTime agora) {
        var t = novo(funcionarioId, data, horaInicio, horaFim, motivo);
        t.autorizar(decisor, agora);
        return t;
    }

    /** Pedido pelo próprio: só para hoje ou para a frente; fica PEDIDO até a chefia ou o RH decidirem. */
    public static TrabalhoSuplementar pedir(FuncionarioId funcionarioId, LocalDate data, LocalTime horaInicio,
                                            LocalTime horaFim, String motivo, LocalDateTime agora) {
        var t = novo(funcionarioId, data, horaInicio, horaFim, motivo);
        if (data.isBefore(agora.toLocalDate()))
            throw invalido("O próprio pede trabalho suplementar para hoje ou para a frente; um dia passado "
                    + "(" + data + ") só a chefia ou o RH o autorizam.");
        t.pedidoPeloProprio = true;
        return t;
    }

    private static TrabalhoSuplementar novo(FuncionarioId funcionarioId, LocalDate data, LocalTime horaInicio,
                                            LocalTime horaFim, String motivo) {
        Objects.requireNonNull(funcionarioId);
        if (data == null) throw invalido("A data do trabalho suplementar é obrigatória.");
        if (horaInicio == null || horaFim == null)
            throw invalido("A hora de início e a hora de fim são obrigatórias (HH:mm).");
        if (!horaInicio.isBefore(horaFim))
            throw invalido("A hora de início (" + horaInicio + ") tem de ser antes da hora de fim (" + horaFim
                    + "); um intervalo que passa a meia-noite regista-se em dois dias.");
        if (motivo == null || motivo.isBlank()) throw invalido("O trabalho suplementar diz porquê: o motivo é obrigatório.");

        var t = new TrabalhoSuplementar();
        t.id = TrabalhoSuplementarId.gerarNovo();
        t.funcionarioId = funcionarioId;
        t.data = data;
        t.horaInicio = horaInicio.withSecond(0).withNano(0);
        t.horaFim = horaFim.withSecond(0).withNano(0);
        t.motivo = motivo.trim();
        t.estado = EstadoTrabalhoSuplementar.PEDIDO;
        return t;
    }

    public static TrabalhoSuplementar reconstruir(TrabalhoSuplementarId id, FuncionarioId funcionarioId, LocalDate data,
                                                  LocalTime horaInicio, LocalTime horaFim, String motivo,
                                                  EstadoTrabalhoSuplementar estado, boolean pedidoPeloProprio,
                                                  boolean autorizacaoPosterior, FuncionarioId decididoPor,
                                                  LocalDateTime decididoEm, String motivoRecusa,
                                                  String motivoCancelamento, LocalDateTime canceladoEm) {
        var t = new TrabalhoSuplementar();
        t.id = id;
        t.funcionarioId = funcionarioId;
        t.data = data;
        t.horaInicio = horaInicio;
        t.horaFim = horaFim;
        t.motivo = motivo;
        t.estado = estado;
        t.pedidoPeloProprio = pedidoPeloProprio;
        t.autorizacaoPosterior = autorizacaoPosterior;
        t.decididoPor = decididoPor;
        t.decididoEm = decididoEm;
        t.motivoRecusa = motivoRecusa;
        t.motivoCancelamento = motivoCancelamento;
        t.canceladoEm = canceladoEm;
        return t;
    }

    /** {@code decisor} nulo quando decide o RH. */
    public void autorizar(FuncionarioId decisor, LocalDateTime agora) {
        exigirPedido();
        this.estado = EstadoTrabalhoSuplementar.AUTORIZADO;
        this.decididoPor = decisor;
        this.decididoEm = agora;
        this.autorizacaoPosterior = data.isBefore(agora.toLocalDate());
    }

    public void recusar(FuncionarioId decisor, String motivo, LocalDateTime agora) {
        exigirPedido();
        if (motivo == null || motivo.isBlank()) throw invalido("Recusar trabalho suplementar exige motivo.");
        this.estado = EstadoTrabalhoSuplementar.RECUSADO;
        this.decididoPor = decisor;
        this.decididoEm = agora;
        this.motivoRecusa = motivo.trim();
    }

    /** Fica, cancelado, com o motivo: um pedido ou uma autorização que já não vale. */
    public void cancelar(String motivo, LocalDateTime agora) {
        if (!emVigor())
            throw IgrpResponseStatusException.conflict("Só se cancela trabalho suplementar pedido ou autorizado. Estado: "
                    + estado + ".");
        if (motivo == null || motivo.isBlank()) throw invalido("Cancelar trabalho suplementar exige motivo.");
        this.estado = EstadoTrabalhoSuplementar.CANCELADO;
        this.motivoCancelamento = motivo.trim();
        this.canceladoEm = agora;
    }

    private void exigirPedido() {
        if (estado != EstadoTrabalhoSuplementar.PEDIDO)
            throw IgrpResponseStatusException.conflict("Só se decide trabalho suplementar pedido. Estado: " + estado + ".");
    }

    /** Pedido ou autorizado: ocupa o intervalo. */
    public boolean emVigor() {
        return estado == EstadoTrabalhoSuplementar.PEDIDO || estado == EstadoTrabalhoSuplementar.AUTORIZADO;
    }

    public boolean isAutorizado() {
        return estado == EstadoTrabalhoSuplementar.AUTORIZADO;
    }

    public boolean sobrepoe(TrabalhoSuplementar outro) {
        return data.equals(outro.data) && horaInicio.isBefore(outro.horaFim) && outro.horaInicio.isBefore(horaFim);
    }

    public int minutosAutorizados() {
        return (int) Duration.between(horaInicio, horaFim).toMinutes();
    }

    public DiaAssiduidade.Periodo intervalo() {
        return new DiaAssiduidade.Periodo(horaInicio, horaFim);
    }

    /** O intervalo toca num bloco do horário desse dia. */
    public boolean tocaEm(List<BlocoHorario> blocosDoDia) {
        return blocosDoDia.stream().anyMatch(b -> horaInicio.isBefore(b.fim()) && b.inicio().isBefore(horaFim));
    }

    /**
     * As horas realizadas: os minutos de presença (pares completos de marcações válidas) dentro do
     * intervalo autorizado. Num dia útil, o que caia num bloco do horário é tempo normal e não conta —
     * o intervalo nasce fora dos blocos, mas o horário pode ter mudado depois.
     */
    public int minutosRealizados(List<DiaAssiduidade.Periodo> presenca, List<BlocoHorario> blocosDoDia) {
        int total = 0;
        for (DiaAssiduidade.Periodo p : presenca) {
            LocalTime de = max(p.entrada(), horaInicio);
            LocalTime ate = min(p.saida(), horaFim);
            if (!de.isBefore(ate)) continue;
            int minutos = (int) Duration.between(de, ate).toMinutes();
            for (BlocoHorario b : blocosDoDia) {
                LocalTime bde = max(de, b.inicio());
                LocalTime bate = min(ate, b.fim());
                if (bde.isBefore(bate)) minutos -= (int) Duration.between(bde, bate).toMinutes();
            }
            total += Math.max(0, minutos);
        }
        return total;
    }

    private static LocalTime max(LocalTime a, LocalTime b) { return a.isAfter(b) ? a : b; }

    private static LocalTime min(LocalTime a, LocalTime b) { return a.isBefore(b) ? a : b; }

    static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
