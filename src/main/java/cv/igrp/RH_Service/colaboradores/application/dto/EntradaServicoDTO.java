package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Os provimentos e os períodos de prova de um colaborador. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class EntradaServicoDTO {
    private String funcionarioId;
    private List<ProvimentoDTO> provimentos = new ArrayList<>();
    private List<PeriodoProvaDTO> periodos = new ArrayList<>();
}
