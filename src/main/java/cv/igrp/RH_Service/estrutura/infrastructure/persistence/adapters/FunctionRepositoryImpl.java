package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.estrutura.domain.filter.FunctionFilter;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.FunctionMapper;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.FunctionEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FunctionRepositoryImpl implements FunctionRepository {

    private final FunctionEntityRepository entityRepository;
    private final FunctionMapper mapper;

    @Transactional
    @Override
    public OrgFunction save(OrgFunction function) {
        FunctionEntity entity = mapper.toEntity(function);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrgFunction> findById(FunctionId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrgFunction> findByCode(String code) {
        return entityRepository.findByCode(code).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrgFunction> findAll(FunctionFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<FunctionEntity> spec = (root, query, cb) -> {
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
    public boolean existsByCodeAndIdNot(String code, FunctionId id) {
        return entityRepository.existsByCodeAndIdNot(code, id.getValor());
    }
}
