package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.VinculoLaboralFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.VinculoLaboralMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.VinculoLaboralEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.VinculoLaboralEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VinculoLaboralRepositoryImpl implements VinculoLaboralRepository {

    private final VinculoLaboralEntityRepository vinculoLaboralEntityRepository;
    private final VinculoLaboralMapper vinculoLaboralMapper;

    @Transactional
    @Override
    public VinculoLaboral save(VinculoLaboral vinculoLaboral) {
        VinculoLaboralEntity entity = vinculoLaboralMapper.toEntity(vinculoLaboral);
        VinculoLaboralEntity saved = vinculoLaboralEntityRepository.save(entity);
        return vinculoLaboralMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<VinculoLaboral> findById(VinculoLaboralId id) {
        return vinculoLaboralEntityRepository.findById(id.getValor())
            .map(vinculoLaboralMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return vinculoLaboralEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<VinculoLaboral> findAll(VinculoLaboralFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<VinculoLaboralEntity> spec = (root, query, cb) -> {
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

        var page = vinculoLaboralEntityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(vinculoLaboralMapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }
}
