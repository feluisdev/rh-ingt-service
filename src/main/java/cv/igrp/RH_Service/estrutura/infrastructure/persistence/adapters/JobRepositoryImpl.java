package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.estrutura.domain.filter.JobFilter;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.JobMapper;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.JobEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.JobEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JobRepositoryImpl implements JobRepository {

    private final JobEntityRepository entityRepository;
    private final JobMapper mapper;

    @Transactional
    @Override
    public Job save(Job job) {
        JobEntity entity = mapper.toEntity(job);
        return mapper.toDomain(entityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Job> findById(JobId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Job> findByCode(String code) {
        return entityRepository.findByCode(code).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Job> findAll(JobFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<JobEntity> spec = (root, query, cb) -> {
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
    public boolean existsByCodeAndIdNot(String code, JobId id) {
        return entityRepository.existsByCodeAndIdNot(code, id.getValor());
    }
}
