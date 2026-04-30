package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.OrganizationalUnitEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrganizationalUnitRepositoryImpl implements OrganizationalUnitRepository {

    private final OrganizationalUnitEntityRepository entityRepository;
    private final OrganizationalUnitMapper mapper;

    @Transactional
    @Override
    public OrganizationalUnit save(OrganizationalUnit unit) {
        OrganizationalUnitEntity entity = mapper.toEntity(unit);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrganizationalUnit> findById(OrganizationalUnitId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrganizationalUnit> findByCode(String code) {
        return entityRepository.findByCode(code).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrganizationalUnit> findAll(OrganizationalUnitFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<OrganizationalUnitEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getIsActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getIsActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            if (filter.getParentUnitId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("parentUnitId"), filter.getParentUnitId()));
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
    public boolean existsByCodeAndIdNot(String code, OrganizationalUnitId id) {
        return entityRepository.existsByCodeAndIdNot(code, id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsActiveChildrenOf(OrganizationalUnitId parentId) {
        return entityRepository.existsByParentUnitIdAndIsActiveTrue(parentId.getValor());
    }
}
