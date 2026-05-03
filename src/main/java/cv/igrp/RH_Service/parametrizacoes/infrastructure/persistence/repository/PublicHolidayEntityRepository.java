package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.PublicHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PublicHolidayEntityRepository extends JpaRepository<PublicHolidayEntity, UUID>, JpaSpecificationExecutor<PublicHolidayEntity> {
    boolean existsByHolidayDateAndIsNational(LocalDate holidayDate, Boolean isNational);
    boolean existsByHolidayDateAndIsNationalTrueAndIsActiveTrue(LocalDate holidayDate);
    List<PublicHolidayEntity> findAllByIsNationalTrueAndIsActiveTrue();
    List<PublicHolidayEntity> findAllByIsActive(Boolean isActive);
    List<PublicHolidayEntity> findAllByIsNational(Boolean isNational);
    List<PublicHolidayEntity> findAllByIsNationalAndIsActive(Boolean isNational, Boolean isActive);

    @Query("SELECT f FROM PublicHolidayEntity f WHERE year(f.holidayDate) = :ano")
    List<PublicHolidayEntity> findAllByAno(@Param("ano") int ano);

    @Query("SELECT f FROM PublicHolidayEntity f WHERE year(f.holidayDate) = :ano AND f.isNational = :isNational")
    List<PublicHolidayEntity> findAllByAnoAndIsNational(@Param("ano") int ano, @Param("isNational") Boolean isNational);

    @Query("SELECT f FROM PublicHolidayEntity f WHERE year(f.holidayDate) = :ano AND f.isActive = :isActive")
    List<PublicHolidayEntity> findAllByAnoAndIsActive(@Param("ano") int ano, @Param("isActive") Boolean isActive);

    @Query("SELECT f FROM PublicHolidayEntity f WHERE year(f.holidayDate) = :ano AND f.isNational = :isNational AND f.isActive = :isActive")
    List<PublicHolidayEntity> findAllByAnoAndIsNationalAndIsActive(@Param("ano") int ano, @Param("isNational") Boolean isNational, @Param("isActive") Boolean isActive);
}
