package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TacticalActivitiesEntityRepository extends
    JpaRepository<TacticalActivitiesEntity, UUID>,
    JpaSpecificationExecutor<TacticalActivitiesEntity>
{

      List<TacticalActivitiesEntity> findAllByFiscalYear(Integer fiscalYear);

      List<TacticalActivitiesEntity> findAllByFiscalYearAndOrganicUnitId(Integer fiscalYear, UUID organicUnitId);

      List<TacticalActivitiesEntity> findAllByAcceptanceStatusAndEndDateBefore(String acceptanceStatus, java.time.LocalDate date);

      // Fase 119 (PRZ-05): responde "que unidades já submeteram PAA neste ano e neste nível" --
      // consumido por TacticalActivityRepositoryImpl.findOrganicUnitIdsWithActivitiesInYear, que
      // por sua vez serve GetPeriodGenerationQueryHandler para cruzar elegíveis com quem já agiu.
      // Não filtra por `status` de propósito: uma actividade em PENDING_TACTICAL já foi
      // submetida por quem a criou -- o que se pergunta aqui é se a unidade agiu, não se a
      // actividade foi aprovada. `paa_level` chega como código em String, não como enum, porque
      // é assim que a coluna está mapeada em TacticalActivitiesEntity.
      @Query("SELECT DISTINCT t.organicUnitId FROM TacticalActivitiesEntity t "
          + "WHERE t.fiscalYear = :year AND t.paaLevel = :paaLevel AND t.organicUnitId IS NOT NULL")
      List<UUID> findDistinctOrganicUnitIdsByFiscalYearAndPaaLevel(
          @Param("year") Integer year, @Param("paaLevel") String paaLevel);

      default TacticalActivitiesEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"TacticalActivitiesEntity not found for id: " + id));
      }

}