package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.TipoAusenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.TipoAusenciaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("colabsTipoAusenciaMapper")
public class TipoAusenciaMapper {

    public TipoAusencia toDomain(TipoAusenciaEntity e) {
        return TipoAusencia.reconstituir(
                TipoAusenciaId.from(e.getId()),
                e.getDescription(), e.getCode(),
                e.getDeductsBalance(), e.getRequiresApproval(),
                e.getMaxDaysPerYear(),
                e.getCategoryOptionId() != null ? e.getCategoryOptionId().toString() : null,
                e.getIsActive());
    }

    public TipoAusenciaEntity toEntity(TipoAusencia t) {
        TipoAusenciaEntity e = new TipoAusenciaEntity();
        e.setId(t.getId().getValor());
        e.setDescription(t.getNome());
        e.setCode(t.getCodigo());
        e.setDeductsBalance(t.getDeductsBalance());
        e.setRequiresApproval(t.getRequiresApproval());
        e.setMaxDaysPerYear(t.getMaxDaysPerYear());
        if (t.getCategoryOptionCkey() != null) {
            try { e.setCategoryOptionId(UUID.fromString(t.getCategoryOptionCkey())); } catch (IllegalArgumentException ignored) {}
        }
        e.setIsActive(t.getIsActive());
        return e;
    }

    public TipoAusenciaResponseDTO toDTO(TipoAusencia t) {
        TipoAusenciaResponseDTO r = new TipoAusenciaResponseDTO();
        r.setId(t.getId().getStringValor());
        r.setNome(t.getNome());
        r.setCodigo(t.getCodigo());
        r.setDeductsBalance(t.getDeductsBalance());
        r.setRequiresApproval(t.getRequiresApproval());
        r.setMaxDaysPerYear(t.getMaxDaysPerYear());
        r.setCategoryOptionCkey(t.getCategoryOptionCkey());
        r.setIsActive(t.getIsActive());
        return r;
    }
}
