package cv.igrp.RH_Service.carreiras.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.carreiras.domain.filter.CategoryFilter;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.mappers.CategoryMapper;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CategoryEntity;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.CategoryEntityRepository;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository.GradeEntityRepository;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.infrastructure.persistence.SearchSpecificationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryEntityRepository entityRepository;
    private final GradeEntityRepository gradeEntityRepository;
    private final CategoryMapper mapper;

    @Transactional
    @Override
    public Category save(Category category) {
        CategoryEntity entity = mapper.toEntity(category);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findById(CategoryId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Category> findAll(CategoryFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<CategoryEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCareerId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("careerId"), filter.getCareerId()));
            }

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates = cb.and(predicates,
                    SearchSpecificationHelper.exactCode(cb, root.get("code"), filter.getCode()));
            }

            if (filter.getNome() != null && !filter.getNome().isBlank()) {
                predicates = cb.and(predicates,
                    SearchSpecificationHelper.nameSimilarity(cb, root.get("name"), filter.getNome()));
            }

            if (filter.getIsActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getIsActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        var page = entityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Category> findByCareerId(CareerId careerId) {
        return entityRepository.findByCareerId(careerId.getValor())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodeAndCareerId(String code, CareerId careerId) {
        return entityRepository.existsByCodeAndCareerId(code, careerId.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodeAndCareerIdAndIdNot(String code, CareerId careerId, CategoryId id) {
        return entityRepository.existsByCodeAndCareerIdAndIdNot(code, careerId.getValor(), id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsActiveGradesByCategoryId(CategoryId categoryId) {
        return gradeEntityRepository.existsByCategoryIdAndIsActiveTrue(categoryId.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public long countByCareerId(CareerId careerId) {
        return entityRepository.countByCareerId(careerId.getValor());
    }
}
