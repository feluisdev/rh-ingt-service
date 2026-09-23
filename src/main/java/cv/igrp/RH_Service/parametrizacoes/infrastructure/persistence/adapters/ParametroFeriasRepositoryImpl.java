package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ParametroFeriasId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ParametroFeriasMapper;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.ParametroFeriasEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ParametroFeriasRepositoryImpl implements ParametroFeriasRepository {

    private final ParametroFeriasEntityRepository entityRepository;
    private final ParametroFeriasMapper mapper;

    @Transactional
    @Override
    public ParametroFerias save(ParametroFerias parametro) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(parametro)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ParametroFerias> findById(ParametroFeriasId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ParametroFerias> findVigenteEm(int ano) {
        return entityRepository.findFirstByVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(ano)
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParametroFerias> findAll() {
        return entityRepository.findAllByOrderByVigenteDesdeAsc().stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByVigenteDesde(int vigenteDesde) {
        return entityRepository.existsByVigenteDesde(vigenteDesde);
    }
}
