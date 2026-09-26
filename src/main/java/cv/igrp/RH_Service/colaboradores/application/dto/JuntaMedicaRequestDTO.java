package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Pedir a junta ou registar o parecer. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class JuntaMedicaRequestDTO {
    private String motivo;
    private String fundamentacao;
    /** O pedido, ou a junta. */
    private LocalDate data;
    private String parecer;
    private Integer diasIncapacidade;
    private String observacoes;
}
