package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.PublicHolidayFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PublicHolidayRepository {
    PublicHoliday save(PublicHoliday publicHoliday);
    Optional<PublicHoliday> findById(PublicHolidayId id);
    boolean existsByHolidayDateAndNational(LocalDate holidayDate, boolean national);
    List<PublicHoliday> findAll(PublicHolidayFilter filter);
}
