package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Registar um provimento. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProvimentoRequestDTO {
    /** NOMEACAO_PROVISORIA, NOMEACAO_DEFINITIVA, COMISSAO_SERVICO, CONTRATO_GESTAO, CONTRATO_ESTAGIO, CONTRATO_INDETERMINADO, CONTRATO_TERMO_CERTO, CONTRATO_TERMO_INCERTO */
    private String modalidade;
    private String despachoNumero;
    private LocalDate despachoData;
    private LocalDate dataPosse;
    private String concursoRef;
    /** Já era definitivo noutra carreira (estágio em comissão de serviço; sem sucesso, regressa). */
    private Boolean vemDeOutraCarreira;
    /** Estágio probatório: o tutor. */
    private String tutorId;
    /** Termo incerto: a duração prevista, para o período experimental. */
    private Integer mesesPrevistos;
    private String observacoes;
}
