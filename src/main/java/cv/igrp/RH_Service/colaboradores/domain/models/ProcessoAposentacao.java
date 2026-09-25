package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.service.RegrasAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoAposentacaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Um processo de aposentação</b> (Lei n.º 20/X/2023, arts. 48.º, 93.º al. b), 173.º–179.º, 189.º;
 * BR-APO-05..12): do pedido (do funcionário ou da Administração) ao despacho, à desligação do serviço
 * (art. 120.º n.º 1 d): inactividade no quadro aguardando aposentação) e à aposentação, que cessa o
 * vínculo. Na pré-aposentação (art. 179.º), a «desligação» é o início da pré-aposentação, com a
 * percentagem da prestação (70–80%); a aposentação vem depois.
 *
 * <p>A pensão é da segurança social: aqui só se regista o acto e as datas.
 */
@Getter
public class ProcessoAposentacao {

    public enum Iniciativa { FUNCIONARIO, ADMINISTRACAO }

    private ProcessoAposentacaoId id;
    private FuncionarioId funcionarioId;
    private ModalidadeAposentacao modalidade;
    private EstadoProcessoAposentacao estado;
    private Iniciativa iniciativa;
    private LocalDate dataPedido;
    private LocalDate dataPrevista;
    private String fundamentacao;
    /** Art. 176.º n.º 2: a antecipada no interesse da Administração depende sempre do acordo do funcionário. */
    private boolean acordoFuncionario;
    private String despachoNumero;
    private LocalDate despachoData;
    private String motivoIndeferimento;
    private LocalDate dataDesligacao;
    /** Pré-aposentação: a prestação em % da remuneração base (art. 179.º n.º 4). */
    private BigDecimal percentagemPrestacao;
    private LocalDate dataAposentacao;
    private String motivoCancelamento;

    private ProcessoAposentacao() {}

    public static ProcessoAposentacao abrir(FuncionarioId funcionarioId, ModalidadeAposentacao modalidade,
                                            Iniciativa iniciativa, LocalDate dataPedido, LocalDate dataPrevista,
                                            String fundamentacao, boolean acordoFuncionario) {
        Objects.requireNonNull(funcionarioId);
        if (modalidade == null)
            throw invalido("Indique a modalidade da aposentação: limite de idade, antecipada a pedido, antecipada no "
                    + "interesse da Administração, invalidez, pré-aposentação ou compulsiva.");
        if (modalidade == ModalidadeAposentacao.ANTECIPADA_INTERESSE_ADMINISTRACAO && !acordoFuncionario)
            throw invalido("A aposentação antecipada no interesse da Administração depende do acordo do funcionário. "
                    + "Registe o acordo antes de abrir o processo.");
        var p = new ProcessoAposentacao();
        p.id = ProcessoAposentacaoId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.modalidade = modalidade;
        p.estado = EstadoProcessoAposentacao.PEDIDO;
        p.iniciativa = iniciativa != null ? iniciativa : Iniciativa.ADMINISTRACAO;
        p.dataPedido = Objects.requireNonNull(dataPedido);
        p.dataPrevista = dataPrevista;
        p.fundamentacao = texto(fundamentacao);
        p.acordoFuncionario = acordoFuncionario;
        return p;
    }

    public static ProcessoAposentacao reconstruir(ProcessoAposentacaoId id, FuncionarioId funcionarioId,
                                                  ModalidadeAposentacao modalidade, EstadoProcessoAposentacao estado,
                                                  Iniciativa iniciativa, LocalDate dataPedido, LocalDate dataPrevista,
                                                  String fundamentacao, boolean acordoFuncionario, String despachoNumero,
                                                  LocalDate despachoData, String motivoIndeferimento, LocalDate dataDesligacao,
                                                  BigDecimal percentagemPrestacao, LocalDate dataAposentacao,
                                                  String motivoCancelamento) {
        var p = new ProcessoAposentacao();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.modalidade = modalidade;
        p.estado = estado;
        p.iniciativa = iniciativa;
        p.dataPedido = dataPedido;
        p.dataPrevista = dataPrevista;
        p.fundamentacao = fundamentacao;
        p.acordoFuncionario = acordoFuncionario;
        p.despachoNumero = despachoNumero;
        p.despachoData = despachoData;
        p.motivoIndeferimento = motivoIndeferimento;
        p.dataDesligacao = dataDesligacao;
        p.percentagemPrestacao = percentagemPrestacao;
        p.dataAposentacao = dataAposentacao;
        p.motivoCancelamento = motivoCancelamento;
        return p;
    }

    /** O despacho que autoriza (art. 175.º n.º 2; art. 179.º n.º 5). */
    public void deferir(String despachoNumero, LocalDate despachoData, LocalDate dataPrevista) {
        exigir(EstadoProcessoAposentacao.PEDIDO, "deferir");
        if (texto(despachoNumero) == null)
            throw invalido("Indique o número do despacho que autoriza a aposentação.");
        this.despachoNumero = despachoNumero.trim();
        this.despachoData = despachoData;
        if (dataPrevista != null) this.dataPrevista = dataPrevista;
        this.estado = EstadoProcessoAposentacao.DEFERIDO;
    }

    public void indeferir(String motivo) {
        exigir(EstadoProcessoAposentacao.PEDIDO, "indeferir");
        if (texto(motivo) == null) throw invalido("Indeferir o pedido de aposentação exige o motivo.");
        this.motivoIndeferimento = motivo.trim();
        this.estado = EstadoProcessoAposentacao.INDEFERIDO;
    }

    /**
     * Desliga do serviço (aguarda a aposentação) ou, na pré-aposentação, inicia-a — com a percentagem da
     * prestação entre 70% e 80%.
     */
    public void desligar(LocalDate data, BigDecimal percentagemPrestacao) {
        exigir(EstadoProcessoAposentacao.DEFERIDO, "desligar do serviço");
        if (data == null) throw invalido("Indique a data da desligação do serviço.");
        if (modalidade == ModalidadeAposentacao.PRE_APOSENTACAO) {
            if (!RegrasAposentacao.prestacaoValida(percentagemPrestacao))
                throw invalido("Na pré-aposentação a prestação fica entre 70% e 80% da remuneração base. Indique a percentagem.");
            this.percentagemPrestacao = percentagemPrestacao;
        }
        this.dataDesligacao = data;
        this.estado = EstadoProcessoAposentacao.DESLIGADO;
    }

    /** A aposentação: o vínculo cessa nesta data. Pode vir directamente do despacho (sem desligação). */
    public void concluir(LocalDate data) {
        if (estado != EstadoProcessoAposentacao.DEFERIDO && estado != EstadoProcessoAposentacao.DESLIGADO)
            throw IgrpResponseStatusException.conflict("Só se conclui um processo de aposentação já deferido.");
        if (data == null) throw invalido("Indique a data da aposentação.");
        if (dataDesligacao != null && data.isBefore(dataDesligacao))
            throw invalido("A data da aposentação não pode ser anterior à desligação do serviço ("
                    + Datas.pt(dataDesligacao) + ").");
        this.dataAposentacao = data;
        this.estado = EstadoProcessoAposentacao.CONCLUIDO;
    }

    /** Desistência ou anulação, antes da desligação do serviço. */
    public void cancelar(String motivo) {
        if (estado != EstadoProcessoAposentacao.PEDIDO && estado != EstadoProcessoAposentacao.DEFERIDO)
            throw IgrpResponseStatusException.conflict(estado == EstadoProcessoAposentacao.DESLIGADO
                    ? "O colaborador já foi desligado do serviço: o processo conclui-se com a aposentação."
                    : "Este processo de aposentação já terminou.");
        if (texto(motivo) == null) throw invalido("Cancelar o processo de aposentação exige o motivo.");
        this.motivoCancelamento = motivo.trim();
        this.estado = EstadoProcessoAposentacao.CANCELADO;
    }

    private void exigir(EstadoProcessoAposentacao esperado, String accao) {
        if (estado != esperado)
            throw IgrpResponseStatusException.conflict("Não é possível " + accao + " este processo de aposentação no estado em que está.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
