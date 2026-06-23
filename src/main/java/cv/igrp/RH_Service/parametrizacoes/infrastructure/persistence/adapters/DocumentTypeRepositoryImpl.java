package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.DocumentTypeEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.infrastructure.persistence.SearchSpecificationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<DocumentType> findById(DocumentTypeId id) {
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
    public PageResult<DocumentType> findAll(DocumentTypeFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());

        Specification<DocumentTypeEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getCodigo() != null && !filter.getCodigo().isBlank()) {
                predicates = cb.and(predicates,
                    SearchSpecificationHelper.exactCode(cb, root.get("codigo"), filter.getCodigo()));
            }

            if (filter.getNome() != null && !filter.getNome().isBlank()) {
                predicates = cb.and(predicates,
                    SearchSpecificationHelper.nameSimilarity(cb, root.get("descricao"), filter.getNome()));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), filter.getActive()));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), true));
            }

            return predicates;
        };

        var page = documentTypeEntityRepository.findAll(spec, pageable);
        var data = page.getContent().stream().map(documentTypeMapper::toDomain).toList();
        return new PageResult<>(data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(),
                page.isFirst(), page.isLast());
    }
}
