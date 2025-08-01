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
    var pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());

    Specification<DocumentoEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getEstado() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), filter.getEstado()));
      } else {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));
      }

      if (filter.getDocumentoId() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("externalId"), filter.getDocumentoId().getValor()));
      }

      if (filter.getIdTipoDocumento() != null) {
        predicates = cb.and(predicates, cb.equal(root.get("idTipoDoc").get("externalId"), filter.getIdTipoDocumento().getValor()));
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
  public Optional<Documento> getById(ExternalID idDocumento) {
    return documentoEntityRepository.findById(idDocumento.getValor())
        .map(documentoMapper::toDomain);
  }

}
