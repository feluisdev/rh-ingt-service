package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** As férias do ano de uma pessoa da equipa directa, para a chefia planear (preferência e marcação). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class FeriasEquipaLinhaDTO {
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    private List<PeriodoFeriasDTO> preferencia;
    private LocalDate preferenciaIndicadaEm;
    /** PROPRIO ou RH. */
    private String preferenciaIndicadaPor;
    private boolean preferenciaForaDePrazo;
    private List<PeriodoFeriasDTO> marcacao;
    private int totalMarcado;
}
