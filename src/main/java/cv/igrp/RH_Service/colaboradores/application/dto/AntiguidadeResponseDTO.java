package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Tempo de serviço de um colaborador a uma data.
 *
 * <p>Traz a conta <b>e</b> o que a justifica: quem discorda de uma antiguidade quer ver que
 * períodos foram descontados e porquê, não só o total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AntiguidadeResponseDTO {

    private String funcionarioId;

    /** Início da contagem — a data de admissão. */
    private LocalDate dataInicio;
    /** Data até à qual se contou. */
    private LocalDate dataReferencia;

    /** Dias de calendário entre as duas datas, extremos incluídos. */
    private long diasTotais;
    /** Dias que a lei manda não contar. */
    private long diasDescontados;
    /** O que conta: total menos descontados. */
    private long diasContados;

    /** O mesmo, em anos, meses e dias — é assim que se lê uma antiguidade. */
    private int anos;
    private int meses;
    private int dias;

    /** Os períodos efectivamente descontados, já unidos. */
    private List<PeriodoAntiguidadeDTO> periodosDescontados;
}
