package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Abrir um processo de aposentação. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ProcessoAposentacaoRequestDTO {
    /** LIMITE_IDADE, ANTECIPADA_PEDIDO, ANTECIPADA_INTERESSE_ADMINISTRACAO, INVALIDEZ, PRE_APOSENTACAO, COMPULSIVA */
    private String modalidade;
    private LocalDate dataPrevista;
    private String fundamentacao;
    /** Obrigatório (true) na antecipada no interesse da Administração. */
    private Boolean acordoFuncionario;
}
