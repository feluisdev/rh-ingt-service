package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.SaldoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SaldoAusenciaMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsSaldoAusenciaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("colabsSaldoAusenciaRepositoryImpl")
@RequiredArgsConstructor
public class SaldoAusenciaRepositoryImpl implements SaldoAusenciaRepository {

    private final ColabsSaldoAusenciaEntityRepository entityRepository;
    private final SaldoAusenciaMapper mapper;

    @Transactional
    @Override
    public SaldoAusencia save(SaldoAusencia saldo) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(saldo)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SaldoAusencia> findById(SaldoAusenciaId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SaldoAusencia> findAllByFuncionarioId(FuncionarioId funcionarioId, SaldoAusenciaFilter filter) {
        Stream<SaldoAusencia> stream = entityRepository.findAllByFuncionarioId(funcionarioId.getValor())
                .stream().map(mapper::toDomain);
        if (filter.getAno() != null)
            stream = stream.filter(s -> s.getAno() == filter.getAno());
        if (filter.getTipoAusenciaId() != null)
            stream = stream.filter(s -> filter.getTipoAusenciaId().equals(s.getTipoAusenciaId().getValor()));
        return stream.toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SaldoAusencia> findByFuncionarioIdAndTipoAusenciaIdAndAno(
            FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano) {
        return entityRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(
                funcionarioId.getValor(), tipoAusenciaId.getValor(), ano).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByFuncionarioIdAndTipoAusenciaIdAndAno(
            FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano) {
        return entityRepository.existsByFuncionarioIdAndTipoAusenciaIdAndAno(
                funcionarioId.getValor(), tipoAusenciaId.getValor(), ano);
    }
}
