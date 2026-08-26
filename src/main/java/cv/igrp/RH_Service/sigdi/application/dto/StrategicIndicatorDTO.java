package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class StrategicIndicatorDTO {

    private UUID id;

    @NotBlank(message = "O campo <title> é obrigatório")
    private String title;

    private String formula;

    private BigDecimal target;

    private String evaluationCriteria;

    private String infoSource;

    private BigDecimal weight;

    private String criteriaSuperado;

    private String criteriaSeguranca;

    private String criteriaAlcancado;

    private String criteriaInsuficiente;
}
