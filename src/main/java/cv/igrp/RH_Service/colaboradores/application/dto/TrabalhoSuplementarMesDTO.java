package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** O trabalho suplementar de um colaborador num mês; os totais contam só os autorizados. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class TrabalhoSuplementarMesDTO {
    private String funcionarioId;
    private String mes;
    private List<TrabalhoSuplementarDTO> trabalhos;
    private int minutosAutorizados;
    private int minutosRealizados;
    private int minutosRealizadosDiaUtil;
    private int minutosRealizadosDescanso;
    private int minutosRealizadosFeriado;
}
