package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository {
    DocumentType save(DocumentType documentType);
    Optional<DocumentType> findById(ExternalID id);
    boolean existsByCodigo(String codigo);
    List<DocumentType> findAll(DocumentTypeFilter filter);
}
