package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Renovar ou cessar a comissão. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ComissaoServicoRequestDTO {
    /** Renovar: o despacho de renovação. */
    private String despacho;
    /** Cessar: ENTIDADE, NOMEADO (com aviso prévio de 60 dias) ou PENA_DISCIPLINAR (sem aviso). */
    private String iniciativa;
    /** Cessar: por omissão, hoje. */
    private LocalDate dataAviso;
    /** Cessar: por omissão, o fim do aviso prévio. */
    private LocalDate dataEfeito;
    /** Cessar. */
    private String motivo;
}
