package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Documento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsDocumentoMapper")
@RequiredArgsConstructor
public class DocumentoMapper {

    private final DocumentTypeRepository documentTypeRepository;

    public Documento toDomain(DocumentoEntity e) {
        return Documento.reconstituir(
                DocumentoId.from(e.getId()),
                e.getReferenceEntity(),
                e.getReferenceId(),
                DocumentTypeId.from(e.getDocumentTypeId()),
                e.getFileKey(), e.getOriginalFilename(),
                e.getContentType(), e.getFileSize(),
                e.getDescription(), e.getIsActive());
    }

    public DocumentoEntity toEntity(Documento d) {
        DocumentoEntity e = new DocumentoEntity();
        e.setId(d.getId().getValor());
        e.setReferenceEntity(d.getReferenceEntity());
        e.setReferenceId(d.getReferenceId());
        e.setDocumentTypeId(d.getDocumentTypeId().getValor());
        e.setFileKey(d.getFileKey());
        e.setOriginalFilename(d.getOriginalFilename());
        e.setContentType(d.getContentType());
        e.setFileSize(d.getFileSize());
        e.setDescription(d.getDescription());
        e.setIsActive(d.getIsActive());
        return e;
    }

    public DocumentoResponseDTO toDTO(Documento d) {
        DocumentoResponseDTO r = new DocumentoResponseDTO();
        r.setId(d.getId().getStringValor());
        r.setFuncionarioId(d.getReferenceId().toString());
        r.setDocumentTypeId(d.getDocumentTypeId().getStringValor());
        r.setOriginalFilename(d.getOriginalFilename());
        r.setContentType(d.getContentType());
        r.setFileSize(d.getFileSize());
        r.setDescription(d.getDescription());
        r.setIsActive(d.getIsActive());
        documentTypeRepository.findById(d.getDocumentTypeId()).ifPresent(tipo -> {
            DocumentTypeResponseDTO dto = new DocumentTypeResponseDTO();
            dto.setId(tipo.getId().getStringValor());
            dto.setCodigo(tipo.getCodigo());
            dto.setDescricao(tipo.getDescricao());
            dto.setAllowedExtensions(tipo.getAllowedExtensions());
            dto.setCategory(tipo.getCategory());
            dto.setIsActive(tipo.isActive());
            r.setDocumentType(dto);
        });
        return r;
    }
}
