package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TipoDocumentoEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentoMapper {


  private final TipoDocumentoMapper tipoDocumentoMapper;

  public DocumentoMapper(TipoDocumentoMapper tipoDocumentoMapper) {
    this.tipoDocumentoMapper = tipoDocumentoMapper;
  }

  public Documento toDocumentoDomain(ObjetoTipo objetoTipo, ExternalID idObjeto, DocumentoRequestDTO dto, TipoDocumento tipoDocumento) {
     var documentIdExternal = dto.getDocumentoId() != null ? ExternalID.from(dto.getDocumentoId()) : null;

    return Documento.criar(
        documentIdExternal,
        dto.getUrl(),
        dto.getObservacao(),
        objetoTipo,
        idObjeto,
        tipoDocumento
    );
  }


  public DocumentoResponseDTO toDTO(Documento documento) {
    if (documento == null) {
      return null;
    }

    var documentoResponseDto = new DocumentoResponseDTO();
    documentoResponseDto.setId(documento.getId());
    documentoResponseDto.setDocumentoId(documento.getExternalId().getStringValor());
    documentoResponseDto.setUrl(documento.getUrl());
    documentoResponseDto.setObservacao(documento.getObservacao());
    documentoResponseDto.setIdTipoDocumento(documento.getTipoDocumento().getExternalId().getStringValor());
    documentoResponseDto.setTipoDocumento(documento.getTipoDocumento().getDescricao());
    documentoResponseDto.setEstado(documento.getEstado().getCode());
    documentoResponseDto.setEstadoDesc(documento.getEstado().getDescription());

    return documentoResponseDto;


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
        ExternalID.from(entity.getObjectId()),
        entity.getEstado(),
        tipoDocumentoMapper.toDomain(entity.getIdTipoDoc())
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
    entity.setObjectId(domain.getObjectId().getValor());
    entity.setEstado(domain.getEstado());
    entity.setEstado(domain.getEstado());
    TipoDocumentoEntity tipoDocumentoEntity = new TipoDocumentoEntity();
    tipoDocumentoEntity.setId(domain.getTipoDocumento().getId());
    tipoDocumentoEntity.setExternalId(domain.getTipoDocumento().getExternalId().getValor());
    entity.setIdTipoDoc(tipoDocumentoEntity);
    entity.setObservacao(domain.getObservacao());
    return entity;
  }

}

