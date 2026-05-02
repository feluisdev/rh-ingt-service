package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaReciboDTO {
    private List<ReciboVencimentoDTO> content;
    private int totalElements;
}
