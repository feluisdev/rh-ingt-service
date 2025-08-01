package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.TipoDocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TipoDocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.TipoDocumentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TipoDocumentoRepositoryImpl implements TipoDocumentoRepository {

  private final TipoDocumentoEntityRepository tipoDocumentoEntityRepository;
  private final TipoDocumentoMapper tipoDocumentoMapper;

  @Transactional
  @Override
  public TipoDocumento save(TipoDocumento tipoDocumento) {
    var entity = tipoDocumentoMapper.toEntity(tipoDocumento);
    TipoDocumentoEntity saved = tipoDocumentoEntityRepository.save(entity);
    return tipoDocumentoMapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<TipoDocumento> getById(Integer id) {
    return tipoDocumentoEntityRepository.findById(id)
        .map(tipoDocumentoMapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<TipoDocumento> getAll() {
    return tipoDocumentoEntityRepository.findAllByEstado(Estado.A)
        .stream()
        .map(tipoDocumentoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<TipoDocumento> getAll(TipoDocumentoFilter filter) {
    var pageable = PageRequest.of(0, 20);

    Specification<TipoDocumentoEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();


      if (filter.getDescricao() != null && !filter.getDescricao().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("descricao")), "%" + filter.getDescricao().trim().toLowerCase() + "%"));
      }

      if (filter.getCodigo() != null && !filter.getCodigo().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(cb.lower(root.get("codigo")), filter.getCodigo().trim().toLowerCase()));
      }

      if (filter.getEstado() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), filter.getEstado()));
      } else {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));
      }


      return predicates;
    };

    var page = tipoDocumentoEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(tipoDocumentoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<TipoDocumento> getByExternalId(ExternalID externalId) {
    return tipoDocumentoEntityRepository.findByExternalId(externalId.getValor())
        .map(tipoDocumentoMapper::toDomain);
  }
}
