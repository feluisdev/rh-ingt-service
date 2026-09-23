package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class MarcacaoRequestDTO {
    /** yyyy-MM-ddTHH:mm */
    private LocalDateTime momento;
    /** ENTRADA ou SAIDA. */
    private String sentido;
    /** Obrigatório quando o dia já tem marcações (é uma correcção). */
    private String motivo;
}
