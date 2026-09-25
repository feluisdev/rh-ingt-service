package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um acto a publicar no Boletim Oficial ou na página electrónica. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PublicacaoOficialDTO {
    private String id;
    /** PROVIMENTO, NOMEACAO, MOBILIDADE, COMISSAO_SERVICO, CONTRATO_GESTAO, CESSACAO, EXONERACAO, PENA_DISCIPLINAR, REABILITACAO, LISTA_ANTIGUIDADE, CONCURSO, OUTRO */
    private String tipoActo;
    /** BOLETIM_OFICIAL ou PAGINA_ELECTRONICA */
    private String meio;
    private String funcionarioId;
    private String nome;
    /** O que o originou (ex.: FACTO_RH). */
    private String referenciaTipo;
    private String referenciaId;
    private String sumario;
    private LocalDate dataActo;
    /** A_PUBLICAR, PUBLICADA, CANCELADA */
    private String estado;
    /** O documento emitido com o extracto (PDF no MinIO). */
    private String extractoId;
    private String serie;
    private String numero;
    private LocalDate dataPublicacao;
    private String motivoCancelamento;
}
