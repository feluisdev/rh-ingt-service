package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsFeriadoEntityRepository extends JpaRepository<FeriadoEntity, UUID> {
    boolean existsByHolidayDateAndIsNationalTrueAndIsActiveTrue(LocalDate holidayDate);
    List<FeriadoEntity> findAllByIsNationalTrueAndIsActiveTrue();
    List<FeriadoEntity> findAllByIsActive(Boolean isActive);
    List<FeriadoEntity> findAllByIsNational(Boolean isNational);
    List<FeriadoEntity> findAllByIsNationalAndIsActive(Boolean isNational, Boolean isActive);

    @Query("SELECT f FROM ColabsFeriadoEntity f WHERE FUNCTION('YEAR', f.holidayDate) = :ano")
    List<FeriadoEntity> findAllByAno(@Param("ano") int ano);

    @Query("SELECT f FROM ColabsFeriadoEntity f WHERE FUNCTION('YEAR', f.holidayDate) = :ano AND f.isNational = :isNational")
    List<FeriadoEntity> findAllByAnoAndIsNational(@Param("ano") int ano, @Param("isNational") Boolean isNational);

    @Query("SELECT f FROM ColabsFeriadoEntity f WHERE FUNCTION('YEAR', f.holidayDate) = :ano AND f.isActive = :isActive")
    List<FeriadoEntity> findAllByAnoAndIsActive(@Param("ano") int ano, @Param("isActive") Boolean isActive);

    @Query("SELECT f FROM ColabsFeriadoEntity f WHERE FUNCTION('YEAR', f.holidayDate) = :ano AND f.isNational = :isNational AND f.isActive = :isActive")
    List<FeriadoEntity> findAllByAnoAndIsNationalAndIsActive(@Param("ano") int ano, @Param("isNational") Boolean isNational, @Param("isActive") Boolean isActive);
}
