package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Um membro do júri. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MembroJuriDTO {
    /** PRESIDENTE, VOGAL, SUPLENTE */
    private String papel;
    private String nome;
    /** Quando é da casa. */
    private String funcionarioId;
}
