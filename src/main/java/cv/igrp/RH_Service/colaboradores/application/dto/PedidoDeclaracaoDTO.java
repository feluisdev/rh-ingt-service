package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um pedido de declaração, com o documento emitido (se já foi). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PedidoDeclaracaoDTO {
    private String id;
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    /** VINCULO, TEMPO_SERVICO, ANTIGUIDADE_CATEGORIA, SITUACAO_FUNCIONAL */
    private String tipo;
    private String finalidade;
    /** PEDIDA, EMITIDA, RECUSADA */
    private String estado;
    private boolean pedidoPeloProprio;
    private LocalDate dataPedido;
    private String motivoRecusa;
    private DocumentoEmitidoDTO documento;
}
