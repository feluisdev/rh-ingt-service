package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TipoDocumentoEntity;
import org.springframework.stereotype.Component;

@Component
public class TipoDocumentoMapper {

    public TipoDocumentoEntity toEntity(TipoDocumento tipoDocumento) {
        if(tipoDocumento==null) {
            return null;
        }
        TipoDocumentoEntity entity = new TipoDocumentoEntity();
        entity.setId(tipoDocumento.getId());
        entity.setDescricao(tipoDocumento.getDescricao());
        entity.setEstado(tipoDocumento.getEstado());
        entity.setCodigo(tipoDocumento.getCodigo());
        entity.setExternalId(tipoDocumento.getExternalId().getValor());
        return entity;
    }


  public TipoDocumentoResponseDTO toDto(TipoDocumento tipoDocumento) {

        if(tipoDocumento==null) {
            return null;
        }
        TipoDocumentoResponseDTO dto = new TipoDocumentoResponseDTO();
        dto.setTipoDocumentoId(tipoDocumento.getExternalId().getStringValor());
        dto.setDescricao(tipoDocumento.getDescricao());
        dto.setCodigo(tipoDocumento.getCodigo());
        dto.setEstado(tipoDocumento.getEstado() != null ? tipoDocumento.getEstado().getCode() : null);
        dto.setEstadoDesc(tipoDocumento.getEstado() != null ? tipoDocumento.getEstado().getDescription() : null);
        return dto;
    }


  public TipoDocumento toDomain(TipoDocumentoEntity tipoDocumentoEntity) {
    if (tipoDocumentoEntity == null) {
      return null;
    }
    return TipoDocumento.reconstruir(
        tipoDocumentoEntity.getId(),
        ExternalID.from(tipoDocumentoEntity.getExternalId()), // assumindo que o campo é String
        tipoDocumentoEntity.getDescricao(),
        tipoDocumentoEntity.getCodigo(),
        tipoDocumentoEntity.getEstado()
    );
  }

}
