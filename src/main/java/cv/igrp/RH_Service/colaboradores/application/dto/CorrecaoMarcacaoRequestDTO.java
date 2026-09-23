package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Pedido de correcção do próprio: uma picagem esquecida; fica PENDENTE até a chefia ou o RH decidirem. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class CorrecaoMarcacaoRequestDTO {
    /** yyyy-MM-ddTHH:mm */
    private LocalDateTime momento;
    private String sentido;
    /** Obrigatório: porquê. */
    private String motivo;
}
