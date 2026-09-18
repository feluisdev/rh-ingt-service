package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;

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

    private PedidoAusencia() {}

    public static PedidoAusencia criar(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId,
                                       LocalDate dataInicio, LocalDate dataFim,
                                       int numeroDias, String motivo) {
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
        return p;
    }

    public static PedidoAusencia reconstituir(PedidoAusenciaId id, FuncionarioId funcionarioId,
                                              TipoAusenciaId tipoAusenciaId, LocalDate dataInicio,
                                              LocalDate dataFim, int numeroDias, String motivo,
                                              String estado, FuncionarioId aprovadoPor,
                                              LocalDate dataDecisao, String observacoesDecisao,
                                              Boolean isActive) {
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
        return p;
    }

    public void aprovar(FuncionarioId aprovadoPorId, LocalDate dataDecisao, String observacoes) {
        if (!EstadoPedidoAusencia.PENDENTE.equals(this.estado))
            throw IgrpResponseStatusException.conflict("Só é possível aprovar pedidos em estado PENDENTE. Estado actual: " + this.estado);
        this.estado = EstadoPedidoAusencia.APROVADO;
        this.aprovadoPor = aprovadoPorId;
        this.dataDecisao = dataDecisao;
        this.observacoesDecisao = observacoes;
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

    /** Nome do estado, para persistência e respostas. */
    public String getEstadoTexto() {
        return EstadoPedidoAusencia.texto(this.estado);
    }
}
