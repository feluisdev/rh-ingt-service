package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.adapter;

import cv.igrp.RH_Service.funcionarios.domain.filter.TipoDocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.DocumentTypeEntityRepository;
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

  private final DocumentTypeEntityRepository tipoDocumentoEntityRepository;
  private final TipoDocumentoMapper tipoDocumentoMapper;

  @Transactional
  @Override
  public TipoDocumento save(TipoDocumento tipoDocumento) {
    var entity = tipoDocumentoMapper.toEntity(tipoDocumento);
    DocumentTypeEntity saved = tipoDocumentoEntityRepository.save(entity);
    return tipoDocumentoMapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public List<TipoDocumento> getAll() {
    return tipoDocumentoEntityRepository.findAllByIsActive(true)
        .stream()
        .map(tipoDocumentoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<TipoDocumento> getAll(TipoDocumentoFilter filter) {
    var pageable = PageRequest.of(0, 20);

    Specification<DocumentTypeEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getDescricao() != null && !filter.getDescricao().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("descricao")), "%" + filter.getDescricao().trim().toLowerCase() + "%"));
      }

      if (filter.getCodigo() != null && !filter.getCodigo().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(cb.lower(root.get("codigo")), filter.getCodigo().trim().toLowerCase()));
      }

      // mapeia filtro de Estado para isActive: Estado.A → true, qualquer outro → false
      if (filter.getEstado() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("isActive"), filter.getEstado().name().equals("A")));
      } else {
        predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
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
  public Optional<TipoDocumento> getById(ExternalID idTipoDocumento) {
    return tipoDocumentoEntityRepository.findById(idTipoDocumento.getValor())
        .map(tipoDocumentoMapper::toDomain);
  }
}
