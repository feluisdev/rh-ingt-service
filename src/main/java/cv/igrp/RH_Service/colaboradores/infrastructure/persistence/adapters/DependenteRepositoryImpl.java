package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Dependente;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsDependenteEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsDependenteRepositoryImpl")
@RequiredArgsConstructor
public class DependenteRepositoryImpl implements DependenteRepository {

    private final ColabsDependenteEntityRepository entityRepository;
    private final DependenteMapper mapper;

    @Transactional
    @Override
    public Dependente save(Dependente dependente) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(dependente)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Dependente> findById(DependenteId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Dependente> findAllByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioId(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }
}
