package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsLicencaMobilidadeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("colabsLicencaMobilidadeRepositoryImpl")
@RequiredArgsConstructor
public class LicencaMobilidadeRepositoryImpl implements LicencaMobilidadeRepository {

    private final ColabsLicencaMobilidadeEntityRepository entityRepository;
    private final LicencaMobilidadeMapper mapper;

    @Transactional
    @Override
    public LicencaMobilidade save(LicencaMobilidade licenca) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(licenca)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LicencaMobilidade> findById(LicencaMobilidadeId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LicencaMobilidade> findAllByFuncionarioId(FuncionarioId funcionarioId, LicencaMobilidadeFilter filter) {
        Stream<LicencaMobilidade> stream = entityRepository.findAllByFuncionarioId(funcionarioId.getValor())
                .stream().map(mapper::toDomain);
        if (Boolean.TRUE.equals(filter.getActive()))
            stream = stream.filter(l -> Boolean.TRUE.equals(l.getIsActive()));
        else if (Boolean.FALSE.equals(filter.getActive()))
            stream = stream.filter(l -> Boolean.FALSE.equals(l.getIsActive()));
        if (filter.getSubtipoId() != null)
            stream = stream.filter(l -> filter.getSubtipoId().equals(l.getSubtipoId().getValor()));
        return stream.toList();
    }
}
