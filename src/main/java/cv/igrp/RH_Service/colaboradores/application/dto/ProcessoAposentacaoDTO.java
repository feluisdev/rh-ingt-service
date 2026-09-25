package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Um processo de aposentação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProcessoAposentacaoDTO {
    private String id;
    private String funcionarioId;
    /** LIMITE_IDADE, ANTECIPADA_PEDIDO, ANTECIPADA_INTERESSE_ADMINISTRACAO, INVALIDEZ, PRE_APOSENTACAO, COMPULSIVA */
    private String modalidade;
    /** PEDIDO, DEFERIDO, INDEFERIDO, DESLIGADO, CONCLUIDO, CANCELADO */
    private String estado;
    /** FUNCIONARIO ou ADMINISTRACAO */
    private String iniciativa;
    private LocalDate dataPedido;
    private LocalDate dataPrevista;
    private String fundamentacao;
    private boolean acordoFuncionario;
    private String despachoNumero;
    private LocalDate despachoData;
    private String motivoIndeferimento;
    private LocalDate dataDesligacao;
    /** Pré-aposentação: % da remuneração base (70 a 80). */
    private BigDecimal percentagemPrestacao;
    private LocalDate dataAposentacao;
    private String motivoCancelamento;
}
