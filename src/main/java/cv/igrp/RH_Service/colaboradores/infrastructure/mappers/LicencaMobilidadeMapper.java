package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaMobilidadeResponse;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.LicencaMobilidadeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsLicencaMobilidadeMapper")
@RequiredArgsConstructor
public class LicencaMobilidadeMapper {

    private final SubtipoLicencaMobilidadeMapper subtipoMapper;
    private final SubtipoLicencaMobilidadeRepository subtipoRepository;

    public LicencaMobilidade toDomain(LicencaMobilidadeEntity e) {
        return LicencaMobilidade.reconstituir(
                LicencaMobilidadeId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                SubtipoLicencaMobilidadeId.from(e.getSubtipoId()),
                e.getDataInicio(), e.getDataFim(),
                e.getEntidadeDestino(), e.getDespachoNumero(),
                e.getObservacoes(), e.getIsActive(),
                e.getStatus(), e.getDestinationUnitId(),
                e.getJustification(), e.getDocumentId(),
                e.getRejectionReason());
    }

    public LicencaMobilidadeEntity toEntity(LicencaMobilidade l) {
        LicencaMobilidadeEntity e = new LicencaMobilidadeEntity();
        e.setId(l.getId().getValor());
        e.setFuncionarioId(l.getFuncionarioId().getValor());
        e.setSubtipoId(l.getSubtipoId().getValor());
        e.setDataInicio(l.getDataInicio());
        e.setDataFim(l.getDataFim());
        e.setEntidadeDestino(l.getEntidadeDestino());
        e.setDespachoNumero(l.getDespachoNumero());
        e.setObservacoes(l.getObservacoes());
        e.setIsActive(l.getIsActive());
        e.setStatus(l.getStatus() != null ? l.getStatus() : "PENDING");
        e.setDestinationUnitId(l.getDestinationUnitId());
        e.setJustification(l.getJustification());
        e.setDocumentId(l.getDocumentId());
        e.setRejectionReason(l.getRejectionReason());
        return e;
    }

    public LicencaMobilidadeResponse toDTO(LicencaMobilidade l) {
        LicencaMobilidadeResponse r = new LicencaMobilidadeResponse();
        r.setId(l.getId().getStringValor());
        r.setFuncionarioId(l.getFuncionarioId().getStringValor());
        r.setSubtipoId(l.getSubtipoId().getStringValor());
        r.setDataInicio(l.getDataInicio());
        r.setDataFim(l.getDataFim());
        r.setEntidadeDestino(l.getEntidadeDestino());
        r.setDespachoNumero(l.getDespachoNumero());
        r.setObservacoes(l.getObservacoes());
        r.setIsActive(l.getIsActive());
        r.setStatus(l.getStatus());
        r.setDestinationUnitId(l.getDestinationUnitId() != null ? l.getDestinationUnitId().toString() : null);
        r.setJustification(l.getJustification());
        r.setRejectionReason(l.getRejectionReason());
        subtipoRepository.findById(l.getSubtipoId())
                .ifPresent(s -> r.setSubtipo(subtipoMapper.toDTO(s)));
        return r;
    }
}
