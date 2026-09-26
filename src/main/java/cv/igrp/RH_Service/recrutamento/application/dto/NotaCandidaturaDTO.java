package cv.igrp.RH_Service.recrutamento.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** A nota de uma candidatura num método. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class NotaCandidaturaDTO {
    private String metodo;
    private BigDecimal nota;
}
