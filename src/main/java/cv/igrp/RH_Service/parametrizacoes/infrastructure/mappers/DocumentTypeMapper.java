package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeMapper {

    public DocumentTypeEntity toEntity(DocumentType domain) {
        if (domain == null) return null;
        DocumentTypeEntity entity = new DocumentTypeEntity();
        entity.setId(domain.getId().getValor());
        entity.setCodigo(domain.getCodigo());
        entity.setDescricao(domain.getDescricao());
        entity.setAllowedExtensions(domain.getAllowedExtensions());
        entity.setCategory(domain.getCategory());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public DocumentType toDomain(DocumentTypeEntity entity) {
        if (entity == null) return null;
        return DocumentType.reconstruir(
            DocumentTypeId.from(entity.getId()),
            entity.getCodigo(),
            entity.getDescricao(),
            entity.getAllowedExtensions(),
            entity.getCategory(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public DocumentTypeResponseDTO toDTO(DocumentType domain) {
        if (domain == null) return null;
        var dto = new DocumentTypeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCodigo(domain.getCodigo());
        dto.setDescricao(domain.getDescricao());
        dto.setAllowedExtensions(domain.getAllowedExtensions());
        dto.setCategory(domain.getCategory());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
