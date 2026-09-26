package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.JuntaMedicaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/**
 * <b>A comissão de verificação de incapacidade</b> (junta médica; Lei n.º 20/X/2023, arts. 121.º e 175.º; BR-SST-16..18): o
 * pedido (doença prolongada, incapacidade, aposentação por invalidez, inaptidão definitiva no exame) e o parecer.
 *
 * <p>PEDIDA → REALIZADA; CANCELADA.
 */
@Getter
public class JuntaMedica {

    public enum Motivo { DOENCA_PROLONGADA, INCAPACIDADE, APOSENTACAO_INVALIDEZ, INAPTIDAO_DEFINITIVA, OUTRO }
    public enum Parecer { APTO, APTO_OUTRAS_FUNCOES, INCAPAZ_TEMPORARIO, INCAPAZ_PERMANENTE }
    public enum Estado { PEDIDA, REALIZADA, CANCELADA }

    private JuntaMedicaId id;
    private FuncionarioId funcionarioId;
    private Motivo motivo;
    private String fundamentacao;
    private LocalDate dataPedido;
    private LocalDate dataJunta;
    private Parecer parecer;
    private Integer diasIncapacidade;
    private String observacoes;
    private Estado estado;

    private JuntaMedica() {}

    public static JuntaMedica pedir(FuncionarioId funcionarioId, Motivo motivo, String fundamentacao, LocalDate data) {
        if (motivo == null) throw invalido("Indique o motivo do pedido de junta.");
        var j = new JuntaMedica();
        j.id = JuntaMedicaId.gerarNovo();
        j.funcionarioId = funcionarioId;
        j.motivo = motivo;
        j.fundamentacao = fundamentacao != null && !fundamentacao.isBlank() ? fundamentacao.trim() : null;
        j.dataPedido = data;
        j.estado = Estado.PEDIDA;
        return j;
    }

    public static JuntaMedica reconstruir(JuntaMedicaId id, FuncionarioId funcionarioId, Motivo motivo, String fundamentacao, LocalDate dataPedido,
                                          LocalDate dataJunta, Parecer parecer, Integer diasIncapacidade, String observacoes, Estado estado) {
        var j = new JuntaMedica();
        j.id = id;
        j.funcionarioId = funcionarioId;
        j.motivo = motivo;
        j.fundamentacao = fundamentacao;
        j.dataPedido = dataPedido;
        j.dataJunta = dataJunta;
        j.parecer = parecer;
        j.diasIncapacidade = diasIncapacidade;
        j.observacoes = observacoes;
        j.estado = estado;
        return j;
    }

    /** O parecer da junta; incapaz temporário leva os dias. */
    public void registarParecer(LocalDate data, Parecer parecer, Integer dias, String observacoes, LocalDate hoje) {
        if (estado != Estado.PEDIDA) throw IgrpResponseStatusException.conflict("Esta junta já teve parecer.");
        if (data == null || data.isBefore(dataPedido) || data.isAfter(hoje)) throw invalido("A junta é depois do pedido e não no futuro.");
        if (parecer == null) throw invalido("Indique o parecer da junta.");
        if (parecer == Parecer.INCAPAZ_TEMPORARIO && (dias == null || dias < 1)) throw invalido("Incapaz temporário: indique os dias.");
        this.dataJunta = data;
        this.parecer = parecer;
        this.diasIncapacidade = parecer == Parecer.INCAPAZ_TEMPORARIO ? dias : null;
        this.observacoes = observacoes != null && !observacoes.isBlank() ? observacoes.trim() : null;
        this.estado = Estado.REALIZADA;
    }

    public void cancelar() {
        if (estado != Estado.PEDIDA) throw IgrpResponseStatusException.conflict("Só se cancela uma junta pedida.");
        this.estado = Estado.CANCELADA;
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
