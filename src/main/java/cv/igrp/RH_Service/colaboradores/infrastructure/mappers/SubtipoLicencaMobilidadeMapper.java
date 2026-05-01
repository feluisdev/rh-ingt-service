package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeResponse;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.SubtipoLicencaMobilidadeEntity;
import org.springframework.stereotype.Component;

@Component("colabsSubtipoLicencaMobilidadeMapper")
public class SubtipoLicencaMobilidadeMapper {

    public SubtipoLicencaMobilidade toDomain(SubtipoLicencaMobilidadeEntity e) {
        return SubtipoLicencaMobilidade.reconstituir(
                SubtipoLicencaMobilidadeId.from(e.getId()),
                e.getDescription(), e.getCode(), e.getRecordType(),
                e.getAffectsPay(), e.getCountsForSeniority(), e.getCanSelfSubmit(),
                e.getIsActive());
    }

    public SubtipoLicencaMobilidadeEntity toEntity(SubtipoLicencaMobilidade s) {
        SubtipoLicencaMobilidadeEntity e = new SubtipoLicencaMobilidadeEntity();
        e.setId(s.getId().getValor());
        e.setDescription(s.getNome());
        e.setCode(s.getCodigo());
        e.setRecordType(s.getRecordType());
        e.setAffectsPay(s.getAffectsPay());
        e.setCountsForSeniority(s.getCountsForSeniority());
        e.setCanSelfSubmit(s.getCanSelfSubmit());
        e.setIsActive(s.getIsActive());
        return e;
    }

    public SubtipoLicencaMobilidadeResponse toDTO(SubtipoLicencaMobilidade s) {
        SubtipoLicencaMobilidadeResponse r = new SubtipoLicencaMobilidadeResponse();
        r.setId(s.getId().getStringValor());
        r.setNome(s.getNome());
        r.setCodigo(s.getCodigo());
        r.setRecordType(s.getRecordType());
        r.setAffectsPay(s.getAffectsPay());
        r.setCountsForSeniority(s.getCountsForSeniority());
        r.setCanSelfSubmit(s.getCanSelfSubmit());
        r.setIsActive(s.getIsActive());
        return r;
    }
}
