package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.IAMUserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IAMUserProfileEntityRepository extends JpaRepository<IAMUserProfileEntity, UUID> {

    Optional<IAMUserProfileEntity> findBySub(String sub);

    Optional<IAMUserProfileEntity> findByEmail(String email);
}
