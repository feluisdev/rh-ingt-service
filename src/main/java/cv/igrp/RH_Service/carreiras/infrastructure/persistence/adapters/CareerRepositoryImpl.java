package cv.igrp.RH_Service.carreiras.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.carreiras.domain.filter.CareerFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CareerMapper;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CareerEntity;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.CareerEntityRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.CategoryEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CareerRepositoryImpl implements CareerRepository {

    private final CareerEntityRepository entityRepository;
    private final CategoryEntityRepository categoryEntityRepository;
    private final CareerMapper mapper;

    @Transactional
    @Override
    public Career save(Career career) {
        CareerEntity entity = mapper.toEntity(career);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Career> findById(CareerId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Career> findByCode(String code) {
        return entityRepository.findByCode(code).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Career> findAll(CareerFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<CareerEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getIsActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getIsActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        return entityRepository.findAll(spec, pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return entityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodeAndIdNot(String code, CareerId id) {
        return entityRepository.existsByCodeAndIdNot(code, id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsActiveCategoriesByCareerId(CareerId careerId) {
        return categoryEntityRepository.existsByCareerIdAndIsActiveTrue(careerId.getValor());
    }
}
