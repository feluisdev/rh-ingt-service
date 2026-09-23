package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.PedidoAusenciaMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsPedidoAusenciaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("colabsPedidoAusenciaRepositoryImpl")
@RequiredArgsConstructor
public class PedidoAusenciaRepositoryImpl implements PedidoAusenciaRepository {

    private final ColabsPedidoAusenciaEntityRepository entityRepository;
    private final PedidoAusenciaMapper mapper;

    @Transactional
    @Override
    public PedidoAusencia save(PedidoAusencia pedido) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(pedido)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PedidoAusencia> findById(PedidoAusenciaId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PedidoAusencia> findAllByFuncionarioId(FuncionarioId funcionarioId, PedidoAusenciaFilter filter) {
        return entityRepository.findAllByFuncionarioFiltrado(
                        funcionarioId.getValor(),
                        filter.getEstado(),
                        filter.getTipoAusenciaId(),
                        filter.getAno())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public int somarDiasNoAno(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano) {
        return entityRepository.somarDiasNoAno(funcionarioId.getValor(), tipoAusenciaId.getValor(), ano);
    }

    @Override
    public int somarDiasNoMes(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano, int mes) {
        return entityRepository.somarDiasNoMes(funcionarioId.getValor(), tipoAusenciaId.getValor(), ano, mes);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsOverlapForFuncionario(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim) {
        return entityRepository.existsOverlap(funcionarioId.getValor(), dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PedidoAusencia> findAprovadosEntre(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim) {
        return entityRepository.findAprovadosEntre(funcionarioId.getValor(), dataInicio, dataFim)
                .stream().map(mapper::toDomain).toList();
    }
}
