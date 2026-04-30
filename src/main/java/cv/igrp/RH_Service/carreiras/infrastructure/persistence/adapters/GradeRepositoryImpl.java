package cv.igrp.RH_Service.carreiras.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.carreiras.domain.filter.GradeFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.GradeMapper;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.GradeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GradeRepositoryImpl implements GradeRepository {

    private final GradeEntityRepository entityRepository;
    private final GradeMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public Grade save(Grade grade) {
        GradeEntity entity = mapper.toEntity(grade);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Grade> findById(GradeId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Grade> findAll(GradeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<GradeEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCategoryId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }

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
    public List<Grade> findByCategoryIdOrderByGradeNumber(CategoryId categoryId) {
        return entityRepository.findByCategoryIdOrderByGradeNumber(categoryId.getValor())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByGradeNumberAndCategoryId(Integer gradeNumber, CategoryId categoryId) {
        return entityRepository.existsByGradeNumberAndCategoryId(gradeNumber, categoryId.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByGradeNumberAndCategoryIdAndIdNot(Integer gradeNumber, CategoryId categoryId, GradeId id) {
        return entityRepository.existsByGradeNumberAndCategoryIdAndIdNot(gradeNumber, categoryId.getValor(), id.getValor());
    }

    @Override
    public boolean isReferencedByActiveAssignment(GradeId gradeId) {
        try {
            Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM employee_professional_assignments WHERE grade_id = ? AND is_active = true)",
                Boolean.class, gradeId.getValor());
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            // table does not exist yet — no active assignments possible
            return false;
        }
    }
}
