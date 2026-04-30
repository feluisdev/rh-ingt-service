package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.PublicHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.UUID;

public interface PublicHolidayEntityRepository extends JpaRepository<PublicHolidayEntity, UUID>, JpaSpecificationExecutor<PublicHolidayEntity> {
    boolean existsByHolidayDateAndIsNational(LocalDate holidayDate, Boolean isNational);
}
