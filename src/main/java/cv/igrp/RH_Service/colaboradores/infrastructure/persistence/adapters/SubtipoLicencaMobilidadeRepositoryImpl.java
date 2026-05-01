package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.SubtipoLicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SubtipoLicencaMobilidadeMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsSubtipoLicencaMobilidadeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsSubtipoLicencaMobilidadeRepositoryImpl")
@RequiredArgsConstructor
public class SubtipoLicencaMobilidadeRepositoryImpl implements SubtipoLicencaMobilidadeRepository {

    private final ColabsSubtipoLicencaMobilidadeEntityRepository entityRepository;
    private final SubtipoLicencaMobilidadeMapper mapper;

    @Transactional
    @Override
    public SubtipoLicencaMobilidade save(SubtipoLicencaMobilidade subtipo) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(subtipo)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SubtipoLicencaMobilidade> findById(SubtipoLicencaMobilidadeId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SubtipoLicencaMobilidade> findAll(SubtipoLicencaMobilidadeFilter filter) {
        if (filter.getActive() != null && filter.getRecordType() != null)
            return entityRepository.findAllByIsActiveAndRecordType(filter.getActive(), filter.getRecordType())
                    .stream().map(mapper::toDomain).toList();
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(mapper::toDomain).toList();
        if (filter.getRecordType() != null)
            return entityRepository.findAllByRecordType(filter.getRecordType()).stream().map(mapper::toDomain).toList();
        return entityRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return entityRepository.existsByCode(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigoAndIdNot(String codigo, SubtipoLicencaMobilidadeId id) {
        return entityRepository.existsByCodeAndIdNot(codigo, id.getValor());
    }
}
