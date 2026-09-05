package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.DadosBancariosResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.DadosBancarios;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DadosBancariosEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsDadosBancariosMapper")
@RequiredArgsConstructor
public class DadosBancariosMapper {

    private final JpaReferences refs;

    public DadosBancarios toDomain(DadosBancariosEntity e) {
        return DadosBancarios.reconstituir(
                DadosBancariosId.from(e.getId()),
                FuncionarioId.from(e.getFuncionario().getId()),
                e.getBanco(), e.getNumeroConta(),
                e.getIban(), e.getNumeroSegurancaSocial(), e.getIsActive());
    }

    public DadosBancariosEntity toEntity(DadosBancarios d) {
        DadosBancariosEntity e = new DadosBancariosEntity();
        e.setId(d.getId().getValor());
        e.setFuncionario(refs.ref(FuncionarioEntity.class, d.getFuncionarioId().getValor()));
        e.setBanco(d.getBanco());
        e.setNumeroConta(d.getNumeroConta());
        e.setIban(d.getIban());
        e.setNumeroSegurancaSocial(d.getNumeroSegurancaSocial());
        e.setIsActive(d.getIsActive());
        return e;
    }

    public DadosBancariosResponseDTO toDTO(DadosBancarios d) {
        DadosBancariosResponseDTO r = new DadosBancariosResponseDTO();
        r.setId(d.getId().getStringValor());
        r.setFuncionarioId(d.getFuncionarioId().getStringValor());
        r.setBanco(d.getBanco());
        r.setNumeroConta(d.getNumeroConta());
        r.setIban(d.getIban());
        r.setNumeroSegurancaSocial(d.getNumeroSegurancaSocial());
        r.setIsActive(d.getIsActive());
        r.setEstadoDesc(Boolean.TRUE.equals(d.getIsActive()) ? "Ativo" : "Inativo");
        return r;
    }
}
