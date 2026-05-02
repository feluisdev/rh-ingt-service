package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StrategyMapLinkEntityRepository extends
        JpaRepository<StrategyMapLinkEntity, UUID>,
        JpaSpecificationExecutor<StrategyMapLinkEntity> {
    Optional<StrategyMapLinkEntity> findBySourceGoalId_IdAndTargetGoalId_Id(UUID sourceGoalId, UUID targetGoalId);

    List<StrategyMapLinkEntity> findBySourceGoalId_IdentityId_IdAndTargetGoalId_IdentityId_Id(UUID identityId,
            UUID identityId2);

    default StrategyMapLinkEntity findByIdOrThrow(UUID id) {
        return this.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,
                        "StrategyMapLinkEntity not found for id: " + id));
    }

}
