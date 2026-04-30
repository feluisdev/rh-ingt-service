package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.PublicHolidayFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.PublicHolidayMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.PublicHolidayEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.PublicHolidayEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PublicHolidayRepositoryImpl implements PublicHolidayRepository {

    private final PublicHolidayEntityRepository publicHolidayEntityRepository;
    private final PublicHolidayMapper publicHolidayMapper;

    @Transactional
    @Override
    public PublicHoliday save(PublicHoliday publicHoliday) {
        PublicHolidayEntity entity = publicHolidayMapper.toEntity(publicHoliday);
        PublicHolidayEntity saved = publicHolidayEntityRepository.save(entity);
        return publicHolidayMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PublicHoliday> findById(PublicHolidayId id) {
        return publicHolidayEntityRepository.findById(id.getValor())
            .map(publicHolidayMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByHolidayDateAndNational(LocalDate holidayDate, boolean national) {
        return publicHolidayEntityRepository.existsByHolidayDateAndIsNational(holidayDate, national);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublicHoliday> findAll(PublicHolidayFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<PublicHolidayEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getYear() != null) {
                LocalDate start = LocalDate.of(filter.getYear(), 1, 1);
                LocalDate end = LocalDate.of(filter.getYear(), 12, 31);
                predicates = cb.and(predicates,
                    cb.between(root.get("holidayDate"), start, end));
            }

            if (filter.getIsNational() != null) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("isNational"), filter.getIsNational()));
            }

            if (filter.getDateFrom() != null) {
                predicates = cb.and(predicates,
                    cb.greaterThanOrEqualTo(root.get("holidayDate"), filter.getDateFrom()));
            }

            if (filter.getDateTo() != null) {
                predicates = cb.and(predicates,
                    cb.lessThanOrEqualTo(root.get("holidayDate"), filter.getDateTo()));
            }

            if (filter.getIsActive() != null) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("isActive"), filter.getIsActive()));
            } else {
                predicates = cb.and(predicates,
                    cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        return publicHolidayEntityRepository.findAll(spec, pageable)
            .stream()
            .map(publicHolidayMapper::toDomain)
            .toList();
    }
}
