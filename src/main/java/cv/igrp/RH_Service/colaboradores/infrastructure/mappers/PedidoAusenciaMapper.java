package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponse;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PedidoAusenciaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsPedidoAusenciaMapper")
@RequiredArgsConstructor
public class PedidoAusenciaMapper {

    private final TipoAusenciaMapper tipoAusenciaMapper;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    public PedidoAusencia toDomain(PedidoAusenciaEntity e) {
        return PedidoAusencia.reconstituir(
                PedidoAusenciaId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                TipoAusenciaId.from(e.getTipoAusenciaId()),
                e.getDataInicio(), e.getDataFim(), e.getNumeroDias(),
                e.getMotivo(), e.getEstado(),
                e.getAprovadoPor() != null ? FuncionarioId.from(e.getAprovadoPor()) : null,
                e.getDataDecisao(), e.getObservacoesDecisao(),
                e.getIsActive());
    }

    public PedidoAusenciaEntity toEntity(PedidoAusencia p) {
        PedidoAusenciaEntity e = new PedidoAusenciaEntity();
        e.setId(p.getId().getValor());
        e.setFuncionarioId(p.getFuncionarioId().getValor());
        e.setTipoAusenciaId(p.getTipoAusenciaId().getValor());
        e.setDataInicio(p.getDataInicio());
        e.setDataFim(p.getDataFim());
        e.setNumeroDias(p.getNumeroDias());
        e.setMotivo(p.getMotivo());
        e.setEstado(p.getEstado());
        e.setAprovadoPor(p.getAprovadoPor() != null ? p.getAprovadoPor().getValor() : null);
        e.setDataDecisao(p.getDataDecisao());
        e.setObservacoesDecisao(p.getObservacoesDecisao());
        e.setIsActive(p.getIsActive());
        return e;
    }

    public PedidoAusenciaResponse toDTO(PedidoAusencia p) {
        PedidoAusenciaResponse r = new PedidoAusenciaResponse();
        r.setId(p.getId().getStringValor());
        r.setFuncionarioId(p.getFuncionarioId().getStringValor());
        r.setDataInicio(p.getDataInicio());
        r.setDataFim(p.getDataFim());
        r.setNumeroDias(p.getNumeroDias());
        r.setMotivo(p.getMotivo());
        r.setEstado(p.getEstado());
        r.setAprovadoPor(p.getAprovadoPor() != null ? p.getAprovadoPor().getStringValor() : null);
        r.setDataDecisao(p.getDataDecisao());
        r.setObservacoesDecisao(p.getObservacoesDecisao());
        r.setIsActive(p.getIsActive());
        tipoAusenciaRepository.findById(p.getTipoAusenciaId())
                .ifPresent(t -> r.setTipoAusencia(tipoAusenciaMapper.toDTO(t)));
        return r;
    }
}
