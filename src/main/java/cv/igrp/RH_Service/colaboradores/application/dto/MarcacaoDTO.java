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
public class MarcacaoDTO {
    private String id;
    private LocalDateTime momento;
    private String sentido;
    private String origem;
    private String motivo;
    private String referenciaExterna;
    private boolean anulada;
    private String motivoAnulacao;
    private LocalDateTime anuladaEm;
}
