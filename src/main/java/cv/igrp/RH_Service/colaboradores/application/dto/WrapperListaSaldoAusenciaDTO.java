package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaSaldoAusenciaDTO {
    private List<SaldoAusenciaResponse> content;
    private int totalElements;
}
