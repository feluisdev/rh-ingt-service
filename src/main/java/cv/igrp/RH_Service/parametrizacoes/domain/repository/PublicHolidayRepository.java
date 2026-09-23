package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.PublicHolidayFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.time.LocalDate;
import java.util.Optional;

public interface PublicHolidayRepository {
    PublicHoliday save(PublicHoliday publicHoliday);
    Optional<PublicHoliday> findById(PublicHolidayId id);
    /**
     * Já há um feriado nacional activo nesse dia (BR-PH-01). Para um {@code recorrente}, «nesse
     * dia» é o mesmo dia e mês em qualquer ano a partir do da data.
     */
    boolean existeNacionalActivoNoDia(LocalDate data, boolean recorrente);
    PageResult<PublicHoliday> findAll(PublicHolidayFilter filter);
}
