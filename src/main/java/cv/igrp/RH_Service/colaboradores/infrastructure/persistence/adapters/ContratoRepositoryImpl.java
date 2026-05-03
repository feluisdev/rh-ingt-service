package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsContratoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsContratoRepositoryImpl")
@RequiredArgsConstructor
public class ContratoRepositoryImpl implements ContratoRepository {

    private final ColabsContratoEntityRepository entityRepository;
    private final ContratoMapper mapper;

    @Transactional
    @Override
    public Contrato save(Contrato c) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(c)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Contrato> findById(ContratoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Contrato> findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioIdOrderByDataInicioDesc(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsActiveByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.existsByFuncionarioIdAndIsActiveTrue(funcionarioId.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public long countActiveByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.countByFuncionarioIdAndIsActiveTrue(funcionarioId.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNumeroContrato(String numeroContrato) {
        return entityRepository.existsByNumeroContrato(numeroContrato);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNumeroContratoAndIdNot(String numeroContrato, ContratoId id) {
        return entityRepository.existsByNumeroContratoAndIdNot(numeroContrato, id.getValor());
    }
}
