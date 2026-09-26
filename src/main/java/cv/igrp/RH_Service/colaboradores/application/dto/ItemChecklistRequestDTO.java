package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Marcar um item. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ItemChecklistRequestDTO {
    /** FEITO, NAO_APLICAVEL, PENDENTE */
    private String estado;
    /** Obrigatória para dar como não aplicável um item obrigatório. */
    private String observacao;
    /** Por omissão, hoje. */
    private LocalDate data;
}
