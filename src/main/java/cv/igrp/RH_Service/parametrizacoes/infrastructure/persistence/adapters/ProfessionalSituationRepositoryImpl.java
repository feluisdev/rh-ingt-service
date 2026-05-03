package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ProfessionalSituationFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ProfessionalSituationMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ProfessionalSituationEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.ProfessionalSituationEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProfessionalSituationRepositoryImpl implements ProfessionalSituationRepository {

    private final ProfessionalSituationEntityRepository professionalSituationEntityRepository;
    private final ProfessionalSituationMapper professionalSituationMapper;

    @Transactional
    @Override
    public ProfessionalSituation save(ProfessionalSituation professionalSituation) {
        ProfessionalSituationEntity entity = professionalSituationMapper.toEntity(professionalSituation);
        ProfessionalSituationEntity saved = professionalSituationEntityRepository.save(entity);
        return professionalSituationMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProfessionalSituation> findById(ProfessionalSituationId id) {
        return professionalSituationEntityRepository.findById(id.getValor())
            .map(professionalSituationMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return professionalSituationEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<ProfessionalSituation> findAll(ProfessionalSituationFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<ProfessionalSituationEntity> spec = (root, query, cb) -> {
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

        var page = professionalSituationEntityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(professionalSituationMapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }
}
