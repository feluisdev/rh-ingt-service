package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import lombok.Getter;

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

    public void incrementarPendentes(int dias) {
        this.diasPendentes += dias;
    }

    public void decrementarPendentes(int dias) {
        this.diasPendentes = Math.max(0, this.diasPendentes - dias);
    }

    public void atualizarDiasDireito(int novosDiasDireito) {
        this.diasDireito = novosDiasDireito;
    }
}
