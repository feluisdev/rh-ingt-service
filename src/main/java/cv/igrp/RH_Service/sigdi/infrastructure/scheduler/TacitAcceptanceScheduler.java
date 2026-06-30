package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.TacticalActivityMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class TacitAcceptanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TacitAcceptanceScheduler.class);

    private final TacticalActivitiesEntityRepository jpaRepository;
    private final TacticalActivityRepository domainRepository;
    private final TacticalActivityMapper mapper;

    public TacitAcceptanceScheduler(TacticalActivitiesEntityRepository jpaRepository,
                                    TacticalActivityRepository domainRepository,
                                    TacticalActivityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.domainRepository = domainRepository;
        this.mapper = mapper;
    }

    /**
     * Runs daily at 1:00 AM.
     * Finds all activities in PENDING_ACCEPTANCE status where the end date has passed,
     * and automatically transitions them to TACITLY_ACCEPTED.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processTacitAcceptances() {
        LOGGER.info("Starting automated job to check and apply PAA tacit acceptances...");

        LocalDate today = LocalDate.now();
        List<TacticalActivitiesEntity> pendingExceeded = jpaRepository
                .findAllByAcceptanceStatusAndEndDateBefore(AcceptanceStatus.PENDING_ACCEPTANCE.getCode(), today);

        if (pendingExceeded.isEmpty()) {
            LOGGER.info("No PAA activities with expired negotiation deadlines found.");
            return;
        }

        LOGGER.info("Found {} PAA activities needing tacit acceptance.", pendingExceeded.size());

        for (TacticalActivitiesEntity entity : pendingExceeded) {
            try {
                TacticalActivity domain = mapper.toDomainFull(entity);
                TacticalActivity updated = domain.applyTacitAcceptance();
                domainRepository.save(updated);
                LOGGER.info("Applied tacit acceptance to PAA Activity ID: {}", entity.getId());
            } catch (Exception e) {
                LOGGER.error("Failed to apply tacit acceptance to PAA Activity ID: {}", entity.getId(), e);
            }
        }

        LOGGER.info("Finished tacit acceptance job.");
    }
}
