package cv.igrp.RH_Service.carreiras.domain.repository;

import cv.igrp.RH_Service.carreiras.domain.filter.CareerFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface CareerRepository {

    Career save(Career career);

    Optional<Career> findById(CareerId id);

    Optional<Career> findByCode(String code);

    PageResult<Career> findAll(CareerFilter filter);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, CareerId id);

    boolean existsActiveCategoriesByCareerId(CareerId careerId);
}
