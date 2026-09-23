package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Faltas de um tipo no mês, na relação mensal. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class RubricaFaltaDTO {
    private String codigo;
    private String nome;
    /** Dias no mês, pela contagem do tipo (pedidos de dias inteiros). */
    private int dias;
    /** Minutos no mês (pedidos em horas). */
    private int minutos;
    private String efeitoRemuneracao;
    /** Só nas injustificadas: PERDA_REMUNERACAO ou DESCONTO_FERIAS, se registada. */
    private String opcaoFaltaInjustificada;
}
