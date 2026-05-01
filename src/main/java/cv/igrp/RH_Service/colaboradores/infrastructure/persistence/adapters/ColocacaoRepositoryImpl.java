package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.ColocacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsColocacaoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("colabsColocacaoRepositoryImpl")
@RequiredArgsConstructor
public class ColocacaoRepositoryImpl implements ColocacaoRepository {

    private final ColabsColocacaoEntityRepository entityRepository;
    private final ColocacaoMapper mapper;

    @Transactional
    @Override
    public Colocacao save(Colocacao colocacao) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(colocacao)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Colocacao> findById(ColocacaoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Colocacao> findAllByFuncionarioId(FuncionarioId funcionarioId, ColocacaoFilter filter) {
        if (Boolean.TRUE.equals(filter.getIsCurrent())) {
            return entityRepository.findByFuncionarioIdAndIsCurrentTrueAndIsActiveTrue(funcionarioId.getValor())
                    .stream().map(mapper::toDomain).toList();
        }
        return entityRepository.findByFuncionarioIdAndIsActiveTrue(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Colocacao> findCurrentByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findFirstByFuncionarioIdAndIsCurrentTrueAndIsActiveTrue(funcionarioId.getValor())
                .map(mapper::toDomain);
    }

    @Transactional
    @Override
    public void fecharColocacaoAtual(FuncionarioId funcionarioId, LocalDate endDate) {
        entityRepository.fecharColocacaoAtual(funcionarioId.getValor(), endDate);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.existsByFuncionarioId(funcionarioId.getValor());
    }
}
