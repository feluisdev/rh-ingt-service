package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** O relatório do tutor, a decisão, a cessação antecipada ou a denúncia. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PeriodoProvaRequestDTO {
    /** POSITIVA ou NEGATIVA (relatório e decisão; na decisão, por omissão a do relatório) */
    private String avaliacao;
    private String fundamentacao;
    private LocalDate data;
    /** Sem sucesso: o estado de cessação (por omissão, o do catálogo). */
    private String workerStateId;
}
