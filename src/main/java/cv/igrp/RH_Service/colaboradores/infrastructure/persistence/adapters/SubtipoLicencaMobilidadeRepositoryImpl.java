package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.SubtipoLicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveMobilitySubtypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.LeaveMobilitySubtypeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsSubtipoLicencaMobilidadeRepositoryImpl")
@RequiredArgsConstructor
public class SubtipoLicencaMobilidadeRepositoryImpl implements SubtipoLicencaMobilidadeRepository {

    private final LeaveMobilitySubtypeEntityRepository entityRepository;

    private SubtipoLicencaMobilidade toDomain(LeaveMobilitySubtypeEntity e) {
        return SubtipoLicencaMobilidade.reconstituir(
                SubtipoLicencaMobilidadeId.from(e.getId()),
                e.getDescription(), e.getCode(), e.getRecordType(),
                e.getAffectsPay(), e.getCountsForSeniority(), e.getCanSelfSubmit(),
                e.getIsActive());
    }

    private LeaveMobilitySubtypeEntity toEntity(SubtipoLicencaMobilidade s) {
        LeaveMobilitySubtypeEntity e = new LeaveMobilitySubtypeEntity();
        e.setId(s.getId().getValor());
        e.setDescription(s.getNome());
        e.setCode(s.getCodigo());
        e.setRecordType(s.getRecordType());
        e.setAffectsPay(s.getAffectsPay());
        e.setCountsForSeniority(s.getCountsForSeniority());
        e.setCanSelfSubmit(s.getCanSelfSubmit());
        e.setIsActive(s.getIsActive());
        return e;
    }

    @Transactional
    @Override
    public SubtipoLicencaMobilidade save(SubtipoLicencaMobilidade subtipo) {
        return toDomain(entityRepository.save(toEntity(subtipo)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SubtipoLicencaMobilidade> findById(SubtipoLicencaMobilidadeId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SubtipoLicencaMobilidade> findAll(SubtipoLicencaMobilidadeFilter filter) {
        if (filter.getActive() != null && filter.getRecordType() != null)
            return entityRepository.findAllByIsActiveAndRecordType(filter.getActive(), filter.getRecordType())
                    .stream().map(this::toDomain).toList();
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(this::toDomain).toList();
        if (filter.getRecordType() != null)
            return entityRepository.findAllByRecordType(filter.getRecordType()).stream().map(this::toDomain).toList();
        return entityRepository.findAll().stream().map(this::toDomain).toList();
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
