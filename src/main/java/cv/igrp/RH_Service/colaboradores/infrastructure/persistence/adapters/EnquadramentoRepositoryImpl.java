package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.EnquadramentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EnquadramentoRepositoryImpl implements EnquadramentoRepository {

    private final EnquadramentoEntityRepository entityRepository;
    private final EnquadramentoMapper mapper;

    @Transactional
    @Override
    public EnquadramentoProfissional save(EnquadramentoProfissional e) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(e)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<EnquadramentoProfissional> findById(EnquadramentoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<EnquadramentoProfissional> findCurrentByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioIdAndIsCurrentTrue(funcionarioId.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EnquadramentoProfissional> findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioIdOrderByDataInicioDesc(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EnquadramentoProfissional> findAllByUnidadeOrganicaIdCoveringYear(UUID unidadeOrganicaId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        return entityRepository.findAllByUnidadeOrganicaIdCoveringRange(unidadeOrganicaId, startOfYear, endOfYear)
                .stream().map(mapper::toDomain).toList();
    }
}
