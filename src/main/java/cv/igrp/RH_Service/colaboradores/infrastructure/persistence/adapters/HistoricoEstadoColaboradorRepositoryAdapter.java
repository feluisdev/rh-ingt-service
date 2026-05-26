package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.HistoricoEstadoColaboradorMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.HistoricoEstadoColaboradorEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoricoEstadoColaboradorRepositoryAdapter implements HistoricoEstadoColaboradorRepository {

    private final HistoricoEstadoColaboradorEntityRepository entityRepository;
    private final HistoricoEstadoColaboradorMapper mapper;

    @Override
    public HistoricoEstadoColaborador save(HistoricoEstadoColaborador historico) {
        var entity = mapper.toEntity(historico);
        var saved = entityRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<HistoricoEstadoColaborador> findAllByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository
                .findAllByFuncionarioIdOrderByDataEfectividadeDesc(funcionarioId.getValor())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
