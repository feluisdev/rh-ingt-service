package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.DocumentTypeEntityRepository;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentTypeRepositoryImpl implements DocumentTypeRepository {

    private final DocumentTypeEntityRepository documentTypeEntityRepository;
    private final DocumentTypeMapper documentTypeMapper;

    @Transactional
    @Override
    public DocumentType save(DocumentType documentType) {
        DocumentTypeEntity entity = documentTypeMapper.toEntity(documentType);
        DocumentTypeEntity saved = documentTypeEntityRepository.save(entity);
        return documentTypeMapper.toDomain(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DocumentType> findById(ExternalID id) {
        return documentTypeEntityRepository.findById(id.getValor())
            .map(documentTypeMapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return documentTypeEntityRepository.existsByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentType> findAll(DocumentTypeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<DocumentTypeEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCodigo() != null && !filter.getCodigo().isBlank()) {
                predicates = cb.and(predicates,
                    cb.like(cb.lower(root.get("codigo")), "%" + filter.getCodigo().trim().toLowerCase() + "%"));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        return documentTypeEntityRepository.findAll(spec, pageable)
            .stream()
            .map(documentTypeMapper::toDomain)
            .toList();
    }
}
