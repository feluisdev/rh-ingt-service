package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ContractTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ContractTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.ContractTypeEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContractTypeRepositoryImpl implements ContractTypeRepository {

    private final ContractTypeEntityRepository contractTypeEntityRepository;
    private final ContractTypeMapper contractTypeMapper;

    @Transactional
    @Override
    public ContractType save(ContractType contractType) {
        ContractTypeEntity entity = contractTypeMapper.toEntity(contractType);
        ContractTypeEntity saved = contractTypeEntityRepository.save(entity);
        return contractTypeMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ContractType> findById(ContractTypeId id) {
        return contractTypeEntityRepository.findById(id.getValor())
            .map(contractTypeMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return contractTypeEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<ContractType> findAll(ContractTypeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<ContractTypeEntity> spec = (root, query, cb) -> {
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

        var page = contractTypeEntityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(contractTypeMapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }
}
