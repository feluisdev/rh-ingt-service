package cv.igrp.RH_Service.carreiras.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class GradeRequestDTO {

    private String categoryId;

    @Min(1)
    private Integer gradeNumber;

    @NotBlank
    @Size(max = 150)
    private String name;

    private BigDecimal salaryIndex;

    private BigDecimal salaryBase;
}
