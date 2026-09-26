package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Um acto do processo disciplinar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ActoDisciplinarDTO {
    /** PARTICIPACAO, INSTAURACAO, NOMEACAO_INSTRUTOR, INICIO_INSTRUCAO, PRORROGACAO_INSTRUCAO, SUSPENSAO_PREVENTIVA, LEVANTAMENTO_SUSPENSAO, ACUSACAO, NOTIFICACAO_ACUSACAO, DEFESA, RELATORIO, DECISAO, NOTIFICACAO_DECISAO, RECURSO, DECISAO_RECURSO, EFEITOS, ARQUIVAMENTO */
    private String tipo;
    private LocalDate data;
    /** O fim do que o acto abre: a suspensão preventiva, o prazo de defesa, o prazo de recurso. */
    private LocalDate dataFim;
    private Integer dias;
    private String pena;
    private Integer duracao;
    private String texto;
}
