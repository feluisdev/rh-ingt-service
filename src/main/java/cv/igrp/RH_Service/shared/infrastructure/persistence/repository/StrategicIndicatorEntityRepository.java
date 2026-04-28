/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface StrategicIndicatorEntityRepository extends JpaRepository<StrategicIndicatorEntity, UUID>, JpaSpecificationExecutor<StrategicIndicatorEntity> {

    List<StrategicIndicatorEntity> findByGoal_Id(UUID goalId);
}
