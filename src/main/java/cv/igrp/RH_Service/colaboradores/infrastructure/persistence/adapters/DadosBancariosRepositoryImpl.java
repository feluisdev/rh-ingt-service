package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.DadosBancarios;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsDadosBancariosEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsDadosBancariosRepositoryImpl")
@RequiredArgsConstructor
public class DadosBancariosRepositoryImpl implements DadosBancariosRepository {

    private final ColabsDadosBancariosEntityRepository entityRepository;
    private final DadosBancariosMapper mapper;

    @Transactional
    @Override
    public DadosBancarios save(DadosBancarios dadosBancarios) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(dadosBancarios)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DadosBancarios> findById(DadosBancariosId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<DadosBancarios> findAllByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario_Id(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DadosBancarios> findActiveByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario_IdAndIsActiveTrue(funcionarioId.getValor())
                .map(mapper::toDomain);
    }
}
