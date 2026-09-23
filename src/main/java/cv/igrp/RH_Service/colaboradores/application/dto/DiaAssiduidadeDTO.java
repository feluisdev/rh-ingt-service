package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DiaAssiduidadeDTO {
    private LocalDate data;
    /** Os pares entrada/saída, calculados das marcações válidas. */
    private List<PeriodoPresencaDTO> periodos = new ArrayList<>();
    /** Minutos de cada intervalo entre dois períodos. */
    private List<Integer> intervalosMinutos = new ArrayList<>();
    private int minutosTrabalhados;
    /** Do horário vigente nesse dia; zero em feriado ou sem horário. */
    private int minutosEsperados;
    private String horarioNome;
    private boolean feriado;
    /** ENTRADA_SEM_SAIDA, SAIDA_SEM_ENTRADA, ENTRADAS_SEGUIDAS: o dia tem de ser corrigido. */
    private List<String> anomalias = new ArrayList<>();
    /** Todas as marcações do dia, anuladas incluídas. */
    private List<MarcacaoDTO> marcacoes = new ArrayList<>();
}
