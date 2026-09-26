package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Uma acumulação de funções. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AcumulacaoFuncoesDTO {
    private String id;
    private String funcionarioId;
    private String funcionarioNome;
    /** PUBLICA, PRIVADA */
    private String tipo;
    /** INERENCIA, REPRESENTACAO, COMISSAO_GRUPO_TRABALHO, ORGAO_COLEGIAL, DOCENCIA_INVESTIGACAO, CONFERENCIA_FORMACAO */
    private String casoPublico;
    private boolean remunerada;
    /** Quem autoriza (art. 23.º): DIRIGENTE_MAXIMO (não remunerada) ou MEMBROS_GOVERNO (remunerada). */
    private String autorizacao;
    private String entidade;
    private String funcoes;
    private String horario;
    private Integer horasSemanais;
    private LocalDate inicio;
    private LocalDate fim;
    private boolean declaracaoSemConflito;
    /** PEDIDA, AUTORIZADA, INDEFERIDA, CESSADA, CADUCADA */
    private String estado;
    private String despacho;
    private LocalDate dataDespacho;
    private String motivo;
    private LocalDate dataFimEfectiva;
}
