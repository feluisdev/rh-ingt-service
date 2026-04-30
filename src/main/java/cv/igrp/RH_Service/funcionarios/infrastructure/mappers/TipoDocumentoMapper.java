package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import org.springframework.stereotype.Component;

@Component
public class TipoDocumentoMapper {

    public DocumentTypeEntity toEntity(TipoDocumento tipoDocumento) {
        if(tipoDocumento==null) {
            return null;
        }
        DocumentTypeEntity entity = new DocumentTypeEntity();
        entity.setId(tipoDocumento.getIdTipoDocumento().getValor());
        entity.setDescricao(tipoDocumento.getDescricao());
        entity.setCodigo(tipoDocumento.getCodigo());
        entity.setIsActive(tipoDocumento.getEstado() == Estado.A);
        return entity;
    }


  public TipoDocumentoResponseDTO toDto(TipoDocumento tipoDocumento) {

        if(tipoDocumento==null) {
            return null;
        }
        TipoDocumentoResponseDTO dto = new TipoDocumentoResponseDTO();
        dto.setTipoDocumentoId(tipoDocumento.getIdTipoDocumento().getStringValor());
        dto.setDescricao(tipoDocumento.getDescricao());
        dto.setCodigo(tipoDocumento.getCodigo());
        dto.setEstado(tipoDocumento.getEstado() != null ? tipoDocumento.getEstado().getCode() : null);
        dto.setEstadoDesc(tipoDocumento.getEstado() != null ? tipoDocumento.getEstado().getDescription() : null);
        return dto;
    }


  public TipoDocumento toDomain(DocumentTypeEntity entity) {
    if (entity == null) {
      return null;
    }
    return TipoDocumento.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getDescricao(),
        entity.getCodigo(),
        Boolean.TRUE.equals(entity.getIsActive()) ? Estado.A : Estado.I
    );
  }

}
