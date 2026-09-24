package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Indicadores do pessoal de um serviço num ano (Lei n.º 20/X/2023, art. 38.º n.º 3). Os números; o gráfico é do front. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class IndicadoresPessoalDTO {
    private int ano;
    /** Hoje, no ano corrente; 31 de Dezembro, num ano passado. */
    private LocalDate referencia;
    private String unidadeId;
    private String unidadeNome;
    private boolean incluirSubunidades;
    /** Quem tinha afectação principal no serviço na referência. */
    private int efectivos;
    private List<ContagemDTO> porGenero;
    private List<ContagemDTO> porEscalaoEtario;
    private List<ContagemDTO> porTipoContrato;
    private List<ContagemDTO> porCarreira;
    private List<ContagemDTO> porUnidade;
    /** Admitidos no ano, entre quem passou pelo serviço. */
    private int entradas;
    /** Com fim do vínculo no ano. */
    private int saidas;
    /** Dias úteis de faltas aprovadas (regime FALTA e FALTA_INJUSTIFICADA; sem férias). */
    private int diasFalta;
    /** Dias úteis de vínculo no ano, até à referência. */
    private int diasUteisPotenciais;
    /** diasFalta / diasUteisPotenciais × 100. */
    private BigDecimal taxaAbsentismo;
    /** Trabalho suplementar realizado no ano (pelas marcações). */
    private int minutosSuplementares;
    private BigDecimal horasSuplementares;
}
