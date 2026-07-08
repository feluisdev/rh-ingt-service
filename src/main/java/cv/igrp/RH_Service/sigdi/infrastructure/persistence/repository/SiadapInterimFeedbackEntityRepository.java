package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SiadapInterimFeedbackEntityRepository extends JpaRepository<SiadapInterimFeedbackEntity, UUID> {
}
