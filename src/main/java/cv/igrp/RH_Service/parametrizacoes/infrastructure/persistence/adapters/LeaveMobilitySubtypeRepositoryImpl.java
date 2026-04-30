package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveMobilitySubtypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveMobilitySubtypeMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveMobilitySubtypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.LeaveMobilitySubtypeEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LeaveMobilitySubtypeRepositoryImpl implements LeaveMobilitySubtypeRepository {

    private final LeaveMobilitySubtypeEntityRepository leaveMobilitySubtypeEntityRepository;
    private final LeaveMobilitySubtypeMapper leaveMobilitySubtypeMapper;

    @Transactional
    @Override
    public LeaveMobilitySubtype save(LeaveMobilitySubtype leaveMobilitySubtype) {
        LeaveMobilitySubtypeEntity entity = leaveMobilitySubtypeMapper.toEntity(leaveMobilitySubtype);
        LeaveMobilitySubtypeEntity saved = leaveMobilitySubtypeEntityRepository.save(entity);
        return leaveMobilitySubtypeMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LeaveMobilitySubtype> findById(LeaveMobilitySubtypeId id) {
        return leaveMobilitySubtypeEntityRepository.findById(id.getValor())
            .map(leaveMobilitySubtypeMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return leaveMobilitySubtypeEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LeaveMobilitySubtype> findAll(LeaveMobilitySubtypeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<LeaveMobilitySubtypeEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates = cb.and(predicates,
                    cb.like(cb.lower(root.get("code")), "%" + filter.getCode().trim().toLowerCase() + "%"));
            }

            if (filter.getRecordType() != null && !filter.getRecordType().isBlank()) {
                predicates = cb.and(predicates,
                    cb.equal(root.get("recordType"), filter.getRecordType().trim()));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        return leaveMobilitySubtypeEntityRepository.findAll(spec, pageable)
            .stream()
            .map(leaveMobilitySubtypeMapper::toDomain)
            .toList();
    }
}
