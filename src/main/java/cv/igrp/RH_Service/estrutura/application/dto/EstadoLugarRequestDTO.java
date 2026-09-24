package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Corpo de congelar e de descongelar um Lugar. O motivo é obrigatório (acto administrativo:
 * falta de dotação, reestruturação…) e não se valida o conteúdo; o despacho é opcional.
 */
@Getter
@Setter
@NoArgsConstructor
public class EstadoLugarRequestDTO {
    private String motivo;
    private String despachoNumero;
}
