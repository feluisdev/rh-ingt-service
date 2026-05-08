package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.WorkerStateMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.WorkerStateEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.WorkerStateEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkerStateRepositoryImpl implements WorkerStateRepository {

    private final WorkerStateEntityRepository workerStateEntityRepository;
    private final WorkerStateMapper workerStateMapper;

    @Transactional
    @Override
    public WorkerState save(WorkerState workerState) {
        WorkerStateEntity entity = workerStateMapper.toEntity(workerState);
        WorkerStateEntity saved = workerStateEntityRepository.save(entity);
        return workerStateMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<WorkerState> findById(WorkerStateId id) {
        return workerStateEntityRepository.findById(id.getValor())
            .map(workerStateMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return workerStateEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public java.util.Optional<WorkerState> findByCode(String code) {
        return workerStateEntityRepository.findByCodeIgnoreCase(code).map(workerStateMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<WorkerState> findAll(WorkerStateFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<WorkerStateEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates = cb.and(predicates,
                    cb.like(cb.lower(root.get("code")), "%" + filter.getCode().trim().toLowerCase() + "%"));
            }

            if (filter.getIsActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getIsActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        var page = workerStateEntityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(workerStateMapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }
}
