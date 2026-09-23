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

    /**
     * Há um feriado nacional activo que já cai em {@code data}: nessa data exacta, ou
     * recorrente no mesmo dia e mês desde um ano não posterior (BR-PH-01, V55).
     */
    @Query("""
            SELECT count(f) > 0 FROM PublicHolidayEntity f
            WHERE f.isActive = true AND f.isNational = true
              AND ( (f.isRecurring = false AND f.holidayDate = :data)
                 OR (f.isRecurring = true AND month(f.holidayDate) = :mes AND day(f.holidayDate) = :dia
                     AND year(f.holidayDate) <= :ano) )""")
    boolean existeNacionalActivoNaData(@Param("data") LocalDate data, @Param("mes") int mes,
                                       @Param("dia") int dia, @Param("ano") int ano);

    /**
     * Um recorrente novo, a partir de {@code ano}, colide com um nacional activo no mesmo dia e
     * mês que seja recorrente (vale sempre) ou pontual num ano igual ou posterior.
     */
    @Query("""
            SELECT count(f) > 0 FROM PublicHolidayEntity f
            WHERE f.isActive = true AND f.isNational = true
              AND month(f.holidayDate) = :mes AND day(f.holidayDate) = :dia
              AND (f.isRecurring = true OR year(f.holidayDate) >= :ano)""")
    boolean existeNacionalActivoNoDiaDoAnoDesde(@Param("mes") int mes, @Param("dia") int dia,
                                                @Param("ano") int ano);

    /**
     * Os feriados activos que podem cair em [{@code inicio}, {@code fim}] para quem trabalha na
     * {@code area} (nula: só os que não têm área). Os pontuais vêm já recortados ao período; os
     * recorrentes vêm todos os que começaram até ao fim dele — projectá-los nos anos do período
     * é aritmética de datas, e faz-se no domínio.
     */
    @Query("""
            SELECT f FROM PublicHolidayEntity f
            WHERE f.isActive = true
              AND (f.areaCkey IS NULL OR f.areaCkey = :area)
              AND ( (f.isRecurring = false AND f.holidayDate BETWEEN :inicio AND :fim)
                 OR (f.isRecurring = true AND f.holidayDate <= :fim) )""")
    List<PublicHolidayEntity> findAplicaveisNoPeriodo(@Param("inicio") LocalDate inicio,
                                                      @Param("fim") LocalDate fim,
                                                      @Param("area") String area);
}
