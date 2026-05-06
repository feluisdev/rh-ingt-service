package cv.igrp.RH_Service.carreiras.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class GradeResponseDTO {
    private String id;
    private String categoryId;
    private String categoryName;
    private Integer gradeNumber;
    private String name;
    private BigDecimal salaryIndex;
    private BigDecimal salaryBase;
    private String careerName;
    private Boolean isActive;
    private String estadoDesc;
}
