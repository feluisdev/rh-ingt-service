package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ParametroFeriasId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ParametroFeriasEntity;
import org.springframework.stereotype.Component;

@Component
public class ParametroFeriasMapper {

    public ParametroFeriasEntity toEntity(ParametroFerias domain) {
        if (domain == null) return null;
        var entity = new ParametroFeriasEntity();
        entity.setId(domain.getId().getValor());
        entity.setVigenteDesde(domain.getVigenteDesde());
        entity.setPrazoPreferencia(ParametroFerias.texto(domain.getPrazoPreferencia()));
        entity.setPrazoMapa(ParametroFerias.texto(domain.getPrazoMapa()));
        entity.setFixacaoInicio(ParametroFerias.texto(domain.getFixacaoInicio()));
        entity.setFixacaoFim(ParametroFerias.texto(domain.getFixacaoFim()));
        entity.setPeriodoMinimoInterpolado(domain.getPeriodoMinimoInterpolado());
        entity.setFundamento(domain.getFundamento());
        return entity;
    }

    public ParametroFerias toDomain(ParametroFeriasEntity entity) {
        if (entity == null) return null;
        return ParametroFerias.reconstruir(
            ParametroFeriasId.from(entity.getId()),
            entity.getVigenteDesde(),
            entity.getPrazoPreferencia(),
            entity.getPrazoMapa(),
            entity.getFixacaoInicio(),
            entity.getFixacaoFim(),
            entity.getPeriodoMinimoInterpolado(),
            entity.getFundamento()
        );
    }

    public ParametroFeriasResponseDTO toDTO(ParametroFerias domain) {
        if (domain == null) return null;
        var dto = new ParametroFeriasResponseDTO();
        dto.setId(domain.isDaLei() ? null : domain.getId().getStringValor());
        dto.setVigenteDesde(domain.getVigenteDesde());
        dto.setPrazoPreferencia(ParametroFerias.texto(domain.getPrazoPreferencia()));
        dto.setPrazoMapa(ParametroFerias.texto(domain.getPrazoMapa()));
        dto.setFixacaoInicio(ParametroFerias.texto(domain.getFixacaoInicio()));
        dto.setFixacaoFim(ParametroFerias.texto(domain.getFixacaoFim()));
        dto.setPeriodoMinimoInterpolado(domain.getPeriodoMinimoInterpolado());
        dto.setFundamento(domain.getFundamento());
        dto.setOrigem(domain.isDaLei() ? "LEI" : "TABELA");
        return dto;
    }
}
