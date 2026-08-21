package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
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

import java.time.LocalDate;
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
    public List<PaaSubmissionPeriod> findAllByYear(Integer year) {
        return jpaRepository.findAllByYear(year)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findActiveByTypeAndPurpose(PaaLevel type, Purpose purpose) {
        // Defensive: (type, purpose) is not enforced unique across overlapping date ranges
        // (see 59-REVIEW.md CR-02) — take the most recently created match instead of assuming
        // a single result, to avoid IncorrectResultSizeDataAccessException.
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        return jpaRepository.findAllActiveByTypeAndPurpose(type.getCode(), purpose.getCode(), today)
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findActiveByTypeAndYearAndPurpose(PaaLevel type, Integer year, Purpose purpose) {
        // Defensive — see comment on findActiveByTypeAndPurpose above.
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        return jpaRepository.findAllActiveByTypeAndYearAndPurpose(type.getCode(), year, purpose.getCode(), today)
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatusAndPurpose(PaaLevel type, Integer year, String status, Purpose purpose) {
        // Defensive — see comment on findActiveByTypeAndPurpose above.
        return jpaRepository.findAllByTypeAndYearAndStatusAndPurpose(type.getCode(), year, status, purpose.getCode())
                .stream()
                .findFirst()
                .map(mapper::toDomain);
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
