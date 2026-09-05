package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ProcessoDisciplinarMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsProcessoDisciplinarEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsProcessoDisciplinarRepositoryImpl")
@RequiredArgsConstructor
public class ProcessoDisciplinarRepositoryImpl implements ProcessoDisciplinarRepository {

    private final ColabsProcessoDisciplinarEntityRepository entityRepository;
    private final ProcessoDisciplinarMapper mapper;

    @Transactional
    @Override
    public ProcessoDisciplinar save(ProcessoDisciplinar processo) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(processo)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProcessoDisciplinar> findById(ProcessoDisciplinarId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessoDisciplinar> findAllByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findAllByFuncionario_Id(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }
}
