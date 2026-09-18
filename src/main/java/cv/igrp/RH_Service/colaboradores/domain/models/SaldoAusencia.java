package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SaldoAusencia {

    private SaldoAusenciaId id;
    private FuncionarioId funcionarioId;
    private TipoAusenciaId tipoAusenciaId;
    private int ano;
    private int diasDireito;
    private int diasGozados;
    private int diasPendentes;

    private SaldoAusencia() {}

    public static SaldoAusencia criar(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId,
                                      int ano, int diasDireito) {
        SaldoAusencia s = new SaldoAusencia();
        s.id = SaldoAusenciaId.gerarNovo();
        s.funcionarioId = funcionarioId;
        s.tipoAusenciaId = tipoAusenciaId;
        s.ano = ano;
        s.diasDireito = diasDireito;
        s.diasGozados = 0;
        s.diasPendentes = 0;
        return s;
    }

    public static SaldoAusencia reconstituir(SaldoAusenciaId id, FuncionarioId funcionarioId,
                                             TipoAusenciaId tipoAusenciaId, int ano,
                                             int diasDireito, int diasGozados, int diasPendentes) {
        SaldoAusencia s = new SaldoAusencia();
        s.id = id;
        s.funcionarioId = funcionarioId;
        s.tipoAusenciaId = tipoAusenciaId;
        s.ano = ano;
        s.diasDireito = diasDireito;
        s.diasGozados = diasGozados;
        s.diasPendentes = diasPendentes;
        return s;
    }

    public int saldoDisponivel() {
        return diasDireito - diasGozados - diasPendentes;
    }

    /**
     * Reserva os dias de um pedido submetido. Fica em <b>pendentes</b> até haver
     * decisão: já não estão disponíveis, mas também ainda não foram gozados.
     */
    public void reservar(int dias) {
        if (dias <= 0) return;
        if (saldoDisponivel() < dias)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saldo insuficiente. Disponível: " + saldoDisponivel() + ", necessário: " + dias + ".");
        this.diasPendentes += dias;
    }

    /** Devolve ao saldo uma reserva que não chegou a ser gozada (rejeição, cancelamento). */
    public void libertarReserva(int dias) {
        this.diasPendentes = Math.max(0, this.diasPendentes - dias);
    }

    /**
     * Aprovação: os dias reservados passam a <b>gozados</b>. Era isto que faltava —
     * o saldo contava sempre 0 dias gozados, por muitos pedidos que fossem deferidos.
     */
    public void confirmarGozo(int dias) {
        if (dias <= 0) return;
        libertarReserva(dias);
        this.diasGozados += dias;
    }

    /** Cancelamento depois de aprovado: os dias gozados voltam ao saldo. */
    public void devolverGozo(int dias) {
        this.diasGozados = Math.max(0, this.diasGozados - dias);
    }


    public void atualizarDiasDireito(int novosDiasDireito) {
        this.diasDireito = novosDiasDireito;
    }
}
