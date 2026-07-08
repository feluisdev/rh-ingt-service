package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.PaaSubmissionPeriodMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.PaaSubmissionPeriodEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaaSubmissionPeriodRepositoryImpl implements PaaSubmissionPeriodRepository {

    private final PaaSubmissionPeriodEntityRepository jpaRepository;
    private final PaaSubmissionPeriodMapper mapper;

    @Transactional
    @Override
    public PaaSubmissionPeriod save(PaaSubmissionPeriod period) {
        PaaSubmissionPeriodEntity entity = mapper.toEntity(period);
        PaaSubmissionPeriodEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findActiveByType(PaaLevel type) {
        return jpaRepository.findActiveByType(type.getCode()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatus(PaaLevel type, Integer year, String status) {
        return jpaRepository.findByTypeAndYearAndStatus(type.getCode(), year, status).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaaSubmissionPeriod> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAll(pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findActiveByTypeAndPurpose(PaaLevel type, Purpose purpose) {
        return jpaRepository.findActiveByTypeAndPurpose(type.getCode(), purpose.getCode()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findActiveByTypeAndYearAndPurpose(PaaLevel type, Integer year, Purpose purpose) {
        return jpaRepository.findActiveByTypeAndYearAndPurpose(type.getCode(), year, purpose.getCode()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatusAndPurpose(PaaLevel type, Integer year, String status, Purpose purpose) {
        return jpaRepository.findByTypeAndYearAndStatusAndPurpose(type.getCode(), year, status, purpose.getCode()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaaSubmissionPeriod> findAllByPurpose(int page, int size, Purpose purpose) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findByPurpose(purpose.getCode(), pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public long countAllByPurpose(Purpose purpose) {
        return jpaRepository.countByPurpose(purpose.getCode());
    }
}
