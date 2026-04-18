package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.KeyResultFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.KeyResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeyResultRepositoryImpl implements KeyResultRepository {

  private final KeyResultsEntityRepository jpaRepository;
  private final KeyResultMapper mapper;

  @Transactional
  @Override
  public KeyResult save(KeyResult keyResult) {
    KeyResultsEntity entity = mapper.toEntity(keyResult);
    KeyResultsEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<KeyResult> findById(KeyResultId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<KeyResult> findByIdFull(KeyResultId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResult<KeyResult> findAll(KeyResultFilter filter) {
    int pageNumber = (filter != null && filter.getPageNumber() != null) ? filter.getPageNumber() : 0;
    int pageSize = (filter != null && filter.getPageSize() != null) ? filter.getPageSize() : 20;

    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Specification<KeyResultsEntity> specification = (root, query, cb) -> cb.conjunction();

    var page = jpaRepository.findAll(specification, pageable);
    List<KeyResult> data = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return new PageResult<>(data, page.getNumber(), page.getSize(), page.getTotalElements(),
        page.getTotalPages(), page.isFirst(), page.isLast());
  }
}
