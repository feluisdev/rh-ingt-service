package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.SaldoAusenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.SaldoAusenciaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("colabsSaldoAusenciaMapper")
@RequiredArgsConstructor
public class SaldoAusenciaMapper {

    private final TipoAusenciaMapper tipoAusenciaMapper;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    public SaldoAusencia toDomain(SaldoAusenciaEntity e) {
        return SaldoAusencia.reconstituir(
                SaldoAusenciaId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                TipoAusenciaId.from(e.getTipoAusenciaId()),
                e.getAno(),
                e.getDiasDireito(),
                e.getDiasGozados(),
                e.getDiasPendentes());
    }

    public SaldoAusenciaEntity toEntity(SaldoAusencia s) {
        SaldoAusenciaEntity e = new SaldoAusenciaEntity();
        e.setId(s.getId().getValor());
        e.setFuncionarioId(s.getFuncionarioId().getValor());
        e.setTipoAusenciaId(s.getTipoAusenciaId().getValor());
        e.setAno(s.getAno());
        e.setDiasDireito(s.getDiasDireito());
        e.setDiasGozados(s.getDiasGozados());
        e.setDiasPendentes(s.getDiasPendentes());
        return e;
    }

    public SaldoAusenciaResponseDTO toDTO(SaldoAusencia s) {
        SaldoAusenciaResponseDTO r = new SaldoAusenciaResponseDTO();
        r.setId(s.getId().getStringValor());
        r.setFuncionarioId(s.getFuncionarioId().getStringValor());
        r.setTipoAusenciaId(s.getTipoAusenciaId().getStringValor());
        r.setAno(s.getAno());
        r.setDiasDireito(s.getDiasDireito());
        r.setDiasGozados(s.getDiasGozados());
        r.setDiasPendentes(s.getDiasPendentes());
        r.setDiasDisponiveis(s.saldoDisponivel());
        tipoAusenciaRepository.findById(s.getTipoAusenciaId())
                .ifPresent(t -> r.setTipoAusencia(tipoAusenciaMapper.toDTO(t)));
        return r;
    }
}
