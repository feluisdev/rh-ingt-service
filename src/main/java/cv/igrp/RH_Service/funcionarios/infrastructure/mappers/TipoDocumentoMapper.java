package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TipoDocumentoEntity;


public class TipoDocumentoMapper {

    public TipoDocumentoEntity toEntity(TipoDocumento tipoDocumento) {
        if(tipoDocumento==null) {
            return null;
        }
        TipoDocumentoEntity entity = new TipoDocumentoEntity();
        entity.setId(tipoDocumento.getId());
        entity.setDescricao(tipoDocumento.getDescricao());
        return entity;
    }


  public TipoDocumentoResponseDTO toDto(TipoDocumento tipoDocumento) {

        if(tipoDocumento==null) {
            return null;
        }
        TipoDocumentoResponseDTO dto = new TipoDocumentoResponseDTO();
        dto.setId(tipoDocumento.getId());
        dto.setDescricao(tipoDocumento.getDescricao());
        return dto;
    }

  public TipoDocumento toDomain(TipoDocumentoResponseDTO dto) {
        if(dto==null) {
            return null;
        }
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(dto.getId());
        tipoDocumento.setDescricao(dto.getDescricao());
        return tipoDocumento;
    }

    public TipoDocumento toDomain(TipoDocumentoEntity dto) {
        if(dto==null) {
            return null;
        }
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(dto.getId());
        tipoDocumento.setDescricao(dto.getDescricao());
        return tipoDocumento;
    }

}
