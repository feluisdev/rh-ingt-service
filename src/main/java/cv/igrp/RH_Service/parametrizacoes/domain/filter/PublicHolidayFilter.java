package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PublicHolidayFilter {
    private Integer year;
    private Boolean isNational;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Boolean isActive;
    private int page = 0;
    private int size = 20;
}
