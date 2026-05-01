package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.TipoAusenciaMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsTipoAusenciaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsTipoAusenciaRepositoryImpl")
@RequiredArgsConstructor
public class TipoAusenciaRepositoryImpl implements TipoAusenciaRepository {

    private final ColabsTipoAusenciaEntityRepository entityRepository;
    private final TipoAusenciaMapper mapper;

    @Transactional
    @Override
    public TipoAusencia save(TipoAusencia tipoAusencia) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(tipoAusencia)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TipoAusencia> findById(TipoAusenciaId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TipoAusencia> findAll(TipoAusenciaFilter filter) {
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(mapper::toDomain).toList();
        return entityRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return entityRepository.existsByCode(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigoAndIdNot(String codigo, TipoAusenciaId id) {
        return entityRepository.existsByCodeAndIdNot(codigo, id.getValor());
    }
}
