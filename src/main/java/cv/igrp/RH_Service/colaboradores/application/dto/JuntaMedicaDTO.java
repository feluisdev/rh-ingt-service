package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um pedido de junta médica (comissão de verificação de incapacidade). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class JuntaMedicaDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** DOENCA_PROLONGADA, INCAPACIDADE, APOSENTACAO_INVALIDEZ, INAPTIDAO_DEFINITIVA, OUTRO */
    private String motivo;
    private String fundamentacao;
    private LocalDate dataPedido;
    private LocalDate dataJunta;
    /** APTO, APTO_OUTRAS_FUNCOES, INCAPAZ_TEMPORARIO, INCAPAZ_PERMANENTE */
    private String parecer;
    private Integer diasIncapacidade;
    private String observacoes;
    /** PEDIDA, REALIZADA, CANCELADA */
    private String estado;
    private List<String> alertas = new ArrayList<>();
}
