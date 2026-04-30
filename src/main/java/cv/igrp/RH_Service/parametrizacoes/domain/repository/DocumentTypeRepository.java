package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository {
    DocumentType save(DocumentType documentType);
    Optional<DocumentType> findById(DocumentTypeId id);
    boolean existsByCodigo(String codigo);
    List<DocumentType> findAll(DocumentTypeFilter filter);
}
