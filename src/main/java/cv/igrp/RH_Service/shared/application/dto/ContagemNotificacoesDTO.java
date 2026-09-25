package cv.igrp.RH_Service.shared.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Quantas notificações estão por ler (o número no sino). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ContagemNotificacoesDTO {
    private long naoLidas;
}
