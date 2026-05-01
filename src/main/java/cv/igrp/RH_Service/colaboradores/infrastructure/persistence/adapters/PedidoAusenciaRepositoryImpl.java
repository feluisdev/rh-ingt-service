package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
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
        Stream<PedidoAusencia> stream = entityRepository.findAllByFuncionarioId(funcionarioId.getValor())
                .stream().map(mapper::toDomain);
        if (filter.getEstado() != null)
            stream = stream.filter(p -> filter.getEstado().equals(p.getEstado()));
        if (filter.getTipoAusenciaId() != null)
            stream = stream.filter(p -> filter.getTipoAusenciaId().equals(p.getTipoAusenciaId().getValor()));
        if (filter.getAno() != null)
            stream = stream.filter(p -> p.getDataInicio().getYear() == filter.getAno());
        return stream.toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsOverlapForFuncionario(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim) {
        return entityRepository.existsOverlap(funcionarioId.getValor(), dataInicio, dataFim);
    }
}
