package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveTypeMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.LeaveTypeEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LeaveTypeRepositoryImpl implements LeaveTypeRepository {

    private final LeaveTypeEntityRepository leaveTypeEntityRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    @Transactional
    @Override
    public LeaveType save(LeaveType leaveType) {
        LeaveTypeEntity entity = leaveTypeMapper.toEntity(leaveType);
        LeaveTypeEntity saved = leaveTypeEntityRepository.save(entity);
        return leaveTypeMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LeaveType> findById(LeaveTypeId id) {
        return leaveTypeEntityRepository.findById(id.getValor())
            .map(leaveTypeMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCode(String code) {
        return leaveTypeEntityRepository.existsByCode(code);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LeaveType> findAll(LeaveTypeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<LeaveTypeEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCode() != null && !filter.getCode().isBlank()) {
                predicates = cb.and(predicates,
                    cb.like(cb.lower(root.get("code")), "%" + filter.getCode().trim().toLowerCase() + "%"));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        return leaveTypeEntityRepository.findAll(spec, pageable)
            .stream()
            .map(leaveTypeMapper::toDomain)
            .toList();
    }
}
