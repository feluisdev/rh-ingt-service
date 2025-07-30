package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentoMapper {
    public DocumentoResponseDTO toDTO(Documento documento) {
        if (documento == null) {
            return null;
        }

        return new DocumentoResponseDTO(
            documento.getId(),
            documento.getExternalId().toString(),
            documento.getUrl(),
            documento.getObservacao(),
            documento.getObjectoTipo(),
            documento.getObjectId().toString(),
            documento.getEstado()
        );


    }
    public Documento toDomain(DocumentoEntity entity) {
        if (entity == null) {
            return null;
        }

        return Documento.reconstruir(
            entity.getId(),
            ExternalID.from(entity.getExternalId()),
            entity.getUrl(),
            entity.getObservacao(),
            entity.getObjectoTipo(),
            entity.getObjectId(),
            entity.getEstado()
        );
    }

    public DocumentoEntity toEntity(Documento domain) {
        if (domain == null) {
            return null;
        }

        DocumentoEntity entity = new DocumentoEntity();

        entity.setId(domain.getId());
      entity.setExternalId(domain.getExternalId().getValor());
        entity.setUrl(domain.getUrl());
        entity.setObjectoTipo(domain.getObjectoTipo());
        entity.setObjectId(domain.getObjectId());
        entity.setEstado(domain.getEstado());
        entity.setEstado(domain.getEstado());
        return entity;
    }

}

