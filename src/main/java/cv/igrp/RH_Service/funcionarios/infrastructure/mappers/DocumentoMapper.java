package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentoMapper {


  public Documento toDocumentoDomain(ObjetoTipo objetoTipo, ExternalID idObjeto, DocumentoRequestDTO dto) {
     var documentIdExternal = dto.getDocumentoId() != null ? ExternalID.from(dto.getDocumentoId()) : null;

    var documento =  Documento.criar(
        documentIdExternal,
        dto.getUrl(),
        dto.getObservacao(),
        objetoTipo,
        idObjeto,
        dto.getTipoDocumento()
    );
    System.out.println("mapper:: "+documento);

    return documento;
  }


  public DocumentoResponseDTO toDTO(Documento documento) {
    if (documento == null) {
      return null;
    }

    var documentoResponseDto = new DocumentoResponseDTO();
    documentoResponseDto.setDocumentoId(documento.getIdDocumento().getStringValor());
    documentoResponseDto.setUrl(documento.getUrl());
    documentoResponseDto.setObservacao(documento.getObservacao());
    documentoResponseDto.setTipoDocumento(documento.getTipoDocumento());
    documentoResponseDto.setEstado(documento.getEstado().getCode());
    documentoResponseDto.setEstadoDesc(documento.getEstado().getDescription());
    return documentoResponseDto;

  }

  public Documento toDomain(DocumentoEntity entity) {
    if (entity == null) {
      return null;
    }

    return Documento.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getUrl(),
        entity.getObservacao(),
        entity.getObjectoTipo(),
        ExternalID.from(entity.getObjectId()),
        entity.getEstado(),
        entity.getTipoDocumento()
    );
  }

  public DocumentoEntity toEntity(Documento domain) {
    if (domain == null) {
      return null;
    }

    DocumentoEntity entity = new DocumentoEntity();
    entity.setId(domain.getIdDocumento().getValor());
    entity.setUrl(domain.getUrl());
    entity.setObjectoTipo(domain.getObjectoTipo());
    entity.setObjectId(domain.getObjectId().getValor());
    entity.setEstado(domain.getEstado());
    entity.setTipoDocumento(domain.getTipoDocumento());
    entity.setObservacao(domain.getObservacao());
    return entity;
  }

}

