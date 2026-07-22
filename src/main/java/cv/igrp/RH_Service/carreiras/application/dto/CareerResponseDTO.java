package cv.igrp.RH_Service.carreiras.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CareerResponseDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private String regime;
    private String regimeDesc;
    private Boolean isActive;
    private String estadoDesc;
    private Long nCategorias;
}
