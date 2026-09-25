package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoDeclaracaoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Um pedido de declaração</b> (BR-DEC-01..04): o colaborador pede em {@code /me} (ou o RH regista), o
 * RH emite — o documento numerado vai para o MinIO — ou recusa com motivo. PEDIDA → EMITIDA · RECUSADA.
 */
@Getter
public class PedidoDeclaracao {

    /** Os tipos de declaração (indústria: as que os serviços de RH mais emitem). */
    public enum Tipo {
        /** Que exerce funções: desde quando, com que vínculo, carreira, categoria, cargo e serviço. */
        VINCULO,
        /** O tempo de serviço contado, já com os descontos da lei. */
        TEMPO_SERVICO,
        /** O tempo na categoria (a mesma contagem da lista de antiguidade). */
        ANTIGUIDADE_CATEGORIA,
        /** O estado e a situação funcional actuais. */
        SITUACAO_FUNCIONAL;

        public static Tipo de(String valor) {
            if (valor == null || valor.isBlank()) return null;
            try {
                return valueOf(valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum Estado { PEDIDA, EMITIDA, RECUSADA }

    private PedidoDeclaracaoId id;
    private FuncionarioId funcionarioId;
    private Tipo tipo;
    /** Para que serve (ex.: «para efeitos de crédito bancário»); entra no documento. */
    private String finalidade;
    private Estado estado;
    private boolean pedidoPeloProprio;
    private LocalDate dataPedido;
    private DocumentoEmitidoId documentoId;
    private String motivoRecusa;

    private PedidoDeclaracao() {}

    public static PedidoDeclaracao pedir(FuncionarioId funcionarioId, Tipo tipo, String finalidade, boolean peloProprio,
                                         LocalDate hoje) {
        Objects.requireNonNull(funcionarioId);
        if (tipo == null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Escolha o tipo de declaração: vínculo, tempo de serviço, antiguidade na categoria ou situação funcional.");
        var p = new PedidoDeclaracao();
        p.id = PedidoDeclaracaoId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.tipo = tipo;
        p.finalidade = finalidade == null || finalidade.isBlank() ? null : finalidade.trim();
        p.estado = Estado.PEDIDA;
        p.pedidoPeloProprio = peloProprio;
        p.dataPedido = Objects.requireNonNull(hoje);
        return p;
    }

    public static PedidoDeclaracao reconstruir(PedidoDeclaracaoId id, FuncionarioId funcionarioId, Tipo tipo, String finalidade,
                                               Estado estado, boolean pedidoPeloProprio, LocalDate dataPedido,
                                               DocumentoEmitidoId documentoId, String motivoRecusa) {
        var p = new PedidoDeclaracao();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.tipo = tipo;
        p.finalidade = finalidade;
        p.estado = estado;
        p.pedidoPeloProprio = pedidoPeloProprio;
        p.dataPedido = dataPedido;
        p.documentoId = documentoId;
        p.motivoRecusa = motivoRecusa;
        return p;
    }

    public void emitida(DocumentoEmitidoId documento) {
        exigirPedida();
        this.documentoId = Objects.requireNonNull(documento);
        this.estado = Estado.EMITIDA;
    }

    public void recusar(String motivo) {
        exigirPedida();
        if (motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Recusar a declaração exige o motivo.");
        this.motivoRecusa = motivo.trim();
        this.estado = Estado.RECUSADA;
    }

    private void exigirPedida() {
        if (estado != Estado.PEDIDA)
            throw IgrpResponseStatusException.conflict("Este pedido de declaração já foi tratado.");
    }
}
