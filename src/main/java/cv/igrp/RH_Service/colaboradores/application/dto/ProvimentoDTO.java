package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Um provimento: a forma de vínculo, o despacho e a posse. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProvimentoDTO {
    private String id;
    private String funcionarioId;
    /** NOMEACAO_PROVISORIA, NOMEACAO_DEFINITIVA, COMISSAO_SERVICO, CONTRATO_GESTAO, CONTRATO_ESTAGIO, CONTRATO_INDETERMINADO, CONTRATO_TERMO_CERTO, CONTRATO_TERMO_INCERTO */
    private String modalidade;
    private String despachoNumero;
    private LocalDate despachoData;
    /** A posse (ou o início de funções): o acto produz efeitos a partir dela. */
    private LocalDate dataPosse;
    private String concursoRef;
    private boolean vemDeOutraCarreira;
    private String periodoProvaId;
    /** O provimento de que este é a continuação (a nomeação definitiva depois do estágio). */
    private String anteriorId;
    private String observacoes;
    private List<String> alertas = new ArrayList<>();
}
