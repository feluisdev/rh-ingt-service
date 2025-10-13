package cv.igrp.RH_Service.options.infrastructure.persistence.repository;


import cv.igrp.RH_Service.options.domain.filter.OptionFilter;
import cv.igrp.RH_Service.options.domain.models.Option;
import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.domain.valueobject.OptionId;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.options.infrastructure.persistence.entity.OptionsEntity;
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

  private final OptionsEntityRepository optionEntityRepository;
  private final OptionMapper optionMapper;

  @Override
  @Transactional
  public Option save(Option option) {
    OptionsEntity entity = optionMapper.toEntity(option);
    return optionMapper.toDomain(optionEntityRepository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Option> findById(OptionId id) {
    return optionEntityRepository.findById(id.getIdentificador().getValor())
        .map(optionMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Option> findAll() {
    return optionEntityRepository.findAllByActiveTrue().stream()
        .map(optionMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Option> findAll(OptionFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<OptionsEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getCcode() != null && !filter.getCcode().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(root.get("ccode"), filter.getCcode().trim()));
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
            cb.equal(root.get("locale"), filter.getLocale().trim()));
      }

      // Ativo por padrão: true. Só mostra inativos se filter.isActive() for false
      boolean activeFilter = filter.isActive();
      predicates = cb.and(predicates, cb.equal(root.get("active"), activeFilter));

      return predicates;
    };

    var page = optionEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(optionMapper::toDomain)
        .toList();
  }

  @Override
  public void delete(OptionId id) {
    throw new UnsupportedOperationException("Delete não implementado ainda");
  }

  @Override
  public boolean existsByCkeyAndCcodeAndLocale(String ckey, String ccode, String locale) {
    return optionEntityRepository.existsByCkeyAndCcodeAndLocale(ckey,ccode,locale);
  }



  @Override
  @Transactional(readOnly = true)
  public boolean existsById(OptionId id) {
    return optionEntityRepository.existsById(id.getIdentificador().getValor());
  }

  @Override
  public boolean existsByCcode(String ccode) {
    return optionEntityRepository.existsByCcode(ccode);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Option> findByCcode(String ccode) {
    return optionEntityRepository.findByCcode(ccode).stream()
        .map(optionMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByCkeyAndCcode(String ckey, String ccode) {
    return optionEntityRepository.existsByCkeyAndCcode(ckey,ccode);
  }
}
