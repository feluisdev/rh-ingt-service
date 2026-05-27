package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class PublicHolidayRequestDTO {
    private String name;
    private LocalDate holidayDate;
    private Boolean isNational;
    private String description;
}
