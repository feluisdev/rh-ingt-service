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
    /** Dias recebidos do ano anterior por acumulação (art. 7.º n.º 1). Ver V50. */
    private int diasAcumulados;
    /** Porque é que esses dias não puderam ser gozados no ano em que se venceram. */
    private String acumulacaoMotivo;
    /** Dias cedidos ao ano seguinte. É o que impede transportar duas vezes o mesmo dia. */
    private int diasTransportados;

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
        s.diasAcumulados = 0;
        s.diasTransportados = 0;
        return s;
    }

    public static SaldoAusencia reconstituir(SaldoAusenciaId id, FuncionarioId funcionarioId,
                                             TipoAusenciaId tipoAusenciaId, int ano,
                                             int diasDireito, int diasGozados, int diasPendentes,
                                             int diasAcumulados, String acumulacaoMotivo,
                                             int diasTransportados) {
        SaldoAusencia s = new SaldoAusencia();
        s.id = id;
        s.funcionarioId = funcionarioId;
        s.tipoAusenciaId = tipoAusenciaId;
        s.ano = ano;
        s.diasDireito = diasDireito;
        s.diasGozados = diasGozados;
        s.diasPendentes = diasPendentes;
        s.diasAcumulados = diasAcumulados;
        s.acumulacaoMotivo = acumulacaoMotivo;
        s.diasTransportados = diasTransportados;
        return s;
    }

    /**
     * O que resta para gozar. Soma os dias recebidos do ano anterior (art. 7.º n.º 1) e desconta
     * os que já foram cedidos ao ano seguinte — sem esse desconto os mesmos dias contariam nos
     * dois anos.
     */
    public int saldoDisponivel() {
        return diasDireito + diasAcumulados - diasGozados - diasPendentes - diasTransportados;
    }

    /**
     * Quantos dias deste ano ainda podem ser acumulados para o seguinte.
     *
     * <p><b>Os dias recebidos do ano anterior não entram.</b> O art. 7.º n.º 1 permite a
     * acumulação «para o ano seguinte», e o art. 8.º n.º 4 manda gozar o remanescente «até ao
     * termo do ano civil imediato»: o horizonte é de um ano, não uma corrente que arrasta dias
     * indefinidamente. Por isso o que se pode ceder é limitado ao direito do próprio ano.
     */
    public int diasAcumulaveis() {
        // O que sobra do direito DESTE ano, depois do que foi gozado, reservado e já cedido.
        // Os dias gozados imputam-se primeiro ao direito do próprio ano; quem gastou tudo o que
        // se venceu neste ano e ainda tem saldo, tem-no à custa dos dias recebidos do ano
        // anterior -- e esses não seguem viagem.
        int sobraDoProprioAno = diasDireito - diasGozados - diasPendentes - diasTransportados;
        return Math.max(0, Math.min(saldoDisponivel(), sobraDoProprioAno));
    }

    /**
     * Cede dias ao ano seguinte (art. 7.º n.º 1). Fica registado na linha de origem para que os
     * mesmos dias não possam ser cedidos outra vez.
     */
    public void transportarParaOAnoSeguinte(int dias) {
        if (dias <= 0)
            throw IgrpResponseStatusException.badRequest("O número de dias a acumular tem de ser positivo.");
        if (dias > diasAcumulaveis())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só há " + diasAcumulaveis() + " dia(s) por gozar neste ano que possam ser acumulados; "
                            + "pedidos " + dias + ".");
        this.diasTransportados += dias;
    }

    /**
     * Recebe os dias do ano anterior, com o motivo por que não puderam ser gozados. O motivo é
     * texto livre — a lei exige que haja um («motivo de serviço»), a instituição é que o escreve.
     */
    public void receberAcumulados(int dias, String motivo) {
        if (dias <= 0)
            throw IgrpResponseStatusException.badRequest("O número de dias a acumular tem de ser positivo.");
        this.diasAcumulados += dias;
        this.acumulacaoMotivo = motivo;
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
