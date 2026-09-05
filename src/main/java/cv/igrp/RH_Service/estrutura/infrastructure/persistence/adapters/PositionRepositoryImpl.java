package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.PositionEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepository {

    private final PositionEntityRepository entityRepository;
    private final PositionMapper mapper;

    @Transactional
    @Override
    public Position save(Position position) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(position)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Position> findById(PositionId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Position> findByNumeroLugar(String numeroLugar) {
        return entityRepository.findByNumeroLugar(numeroLugar).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Position> findResponsavelDeUnidade(UUID unidadeOrganicaId) {
        return entityRepository.findByManagesUnit_IdAndIsActiveTrue(unidadeOrganicaId).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Position> findByUnidade(UUID unidadeOrganicaId) {
        return entityRepository.findByUnidadeOrganica_IdAndIsActiveTrue(unidadeOrganicaId)
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNumeroLugar(String numeroLugar) {
        return entityRepository.existsByNumeroLugar(numeroLugar);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNumeroLugarAndIdNot(String numeroLugar, PositionId id) {
        return entityRepository.existsByNumeroLugarAndIdNot(numeroLugar, id.getValor());
    }
}
