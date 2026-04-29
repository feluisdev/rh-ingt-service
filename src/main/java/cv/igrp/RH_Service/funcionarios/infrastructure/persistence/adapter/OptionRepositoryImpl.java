package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.adapter;

import cv.igrp.RH_Service.funcionarios.domain.filter.OptionFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Option;
import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.OptionEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.OptionEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OptionRepositoryImpl implements OptionRepository {

  private final OptionEntityRepository optionEntityRepository;
  private final OptionMapper optionMapper;

  @Transactional
  @Override
  public Option save(Option option) {
    var entity = optionMapper.toEntity(option);
    var saved = optionEntityRepository.save(entity);
    return optionMapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Option> getById(ExternalID optionId) {
    return optionEntityRepository.findById(optionId.getValor())
        .map(optionMapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Option> getAll() {
    return optionEntityRepository.findAll()
        .stream()
        .map(optionMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Option> getAll(OptionFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<OptionEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getCcode() != null && !filter.getCcode().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("ccode")), "%" + filter.getCcode().trim().toLowerCase() + "%"));
      }

      if (filter.getCkey() != null && !filter.getCkey().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("ckey")), "%" + filter.getCkey().trim().toLowerCase() + "%"));
      }

      if (filter.getCvalue() != null && !filter.getCvalue().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("cvalue")), "%" + filter.getCvalue().trim().toLowerCase() + "%"));
      }

      if (filter.getLocale() != null && !filter.getLocale().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(cb.lower(root.get("locale")), filter.getLocale().trim().toLowerCase()));
      }

      if (filter.getSort_order() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("sortOrder"), filter.getSort_order()));
      }

      if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("description")), "%" + filter.getDescription().trim().toLowerCase() + "%"));
      }

      // Default to active=true when no explicit filter capability is provided
      predicates = cb.and(predicates, cb.equal(root.get("active"), true));

      return predicates;
    };

    var page = optionEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(optionMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByCkeyAndCcodeAndLocale(String ckey, String ccode, String locale) {
    return optionEntityRepository.existsByCkeyAndCcodeAndLocale(ckey, ccode, locale);
  }
}
