package cv.igrp.RH_Service.carreiras.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryResponseDTO {
    private String id;
    private String careerId;
    private String careerName;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
}
