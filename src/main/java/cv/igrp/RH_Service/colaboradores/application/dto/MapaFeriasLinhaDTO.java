package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Uma linha do mapa de férias: um colaborador e o que tem marcado. */
@Data
public class MapaFeriasLinhaDTO {
    private String funcionarioId;
    private String numeroFuncionario;
    private String nome;
    private String origem;
    private List<PeriodoFeriasDTO> periodos = new ArrayList<>();
    private int totalMarcado;
    private boolean temPreferencia;
}
