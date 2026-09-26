package cv.igrp.RH_Service.formacao.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Criar o plano, identificar uma necessidade ou aprovar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PlanoFormacaoRequestDTO {
    /** Criar. */
    private Integer ano;
    /** Criar. */
    private String unidadeId;
    /** Criar. */
    private String designacao;
    /** Necessidade. */
    private String tema;
    /** Necessidade: de quem é (em /me, vazio = o próprio). */
    private String funcionarioId;
    /** Necessidade: ALTA, MEDIA, BAIXA. */
    private String prioridade;
    /** Necessidade. */
    private String justificacao;
    /** Aprovar. */
    private String despacho;
    /** Aprovar. */
    private LocalDate data;
}
