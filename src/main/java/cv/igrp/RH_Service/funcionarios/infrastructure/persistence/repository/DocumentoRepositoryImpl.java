package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DocumentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentoRepositoryImpl implements DocumentoRepository {

  private final DocumentoEntityRepository documentoEntityRepository;
  private final DocumentoMapper documentoMapper;

  @Transactional
  @Override
  public Documento save(Documento documento) {
    var entity = documentoMapper.toEntity(documento);
    var saved = documentoEntityRepository.save(entity);
    return documentoMapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Documento> getById(Integer id) {
    return documentoEntityRepository.findById(id)
        .map(documentoMapper::toDomain);
  }
  @Override
  public List<Documento> getAll() {
    return documentoEntityRepository.findAll ( )
        .stream()
        .map(documentoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Documento> getAll(DocumentoFilter filter) {
    var pageable = PageRequest.of(0, 20); // ajustar se houver paginação em DocumentoFilter

    Specification<DocumentoEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getId() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("id"), filter.getId()));
      }

      if (filter.getExternalId() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("externalId"), filter.getExternalId()));
      }

      if (filter.getUrl() != null && !filter.getUrl().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("url")), "%" + filter.getUrl().trim().toLowerCase() + "%"));
      }

      if (filter.getObservacao() != null && !filter.getObservacao().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("observacao")), "%" + filter.getObservacao().trim().toLowerCase() + "%"));
      }

      if (filter.getObjectoTipo() != null && !filter.getObjectoTipo().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(cb.lower(root.get("objectoTipo")), filter.getObjectoTipo().trim().toLowerCase()));
      }

      if (filter.getObjectId() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("objectId"), filter.getObjectId()));
      }

      if (filter.getEstado() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), filter.getEstado()));
      } else {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));
      }

      return predicates;
    };

    var page = documentoEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(documentoMapper::toDomain)
        .toList();
  }



  @Transactional(readOnly = true)
  @Override
  public Optional<Documento> getByExternalId(ExternalID externalId) {
    return documentoEntityRepository.findByExternalId(externalId.getValor())
        .map(documentoMapper::toDomain);
  }

}
