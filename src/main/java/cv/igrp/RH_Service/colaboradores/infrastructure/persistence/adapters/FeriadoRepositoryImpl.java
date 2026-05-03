package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.FeriadoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.PublicHolidayEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.PublicHolidayEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("colabsFeriadoRepositoryImpl")
@RequiredArgsConstructor
public class FeriadoRepositoryImpl implements FeriadoRepository {

    private final PublicHolidayEntityRepository entityRepository;

    private Feriado toDomain(PublicHolidayEntity e) {
        return Feriado.reconstituir(
                FeriadoId.from(e.getId()),
                e.getName(), e.getHolidayDate(),
                e.getIsNational(), e.getDescription(),
                e.getIsActive());
    }

    private PublicHolidayEntity toEntity(Feriado f) {
        PublicHolidayEntity e = new PublicHolidayEntity();
        e.setId(f.getId().getValor());
        e.setName(f.getNome());
        e.setHolidayDate(f.getData());
        e.setIsNational(f.getIsNational());
        e.setDescription(f.getMunicipioCkey());
        e.setIsActive(f.getIsActive());
        return e;
    }

    @Transactional
    @Override
    public Feriado save(Feriado feriado) {
        return toDomain(entityRepository.save(toEntity(feriado)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Feriado> findById(FeriadoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Feriado> findAll(FeriadoFilter filter) {
        if (filter.getAno() != null && filter.getIsNational() != null && filter.getActive() != null)
            return entityRepository.findAllByAnoAndIsNationalAndIsActive(filter.getAno(), filter.getIsNational(), filter.getActive())
                    .stream().map(this::toDomain).toList();
        if (filter.getAno() != null && filter.getIsNational() != null)
            return entityRepository.findAllByAnoAndIsNational(filter.getAno(), filter.getIsNational())
                    .stream().map(this::toDomain).toList();
        if (filter.getAno() != null && filter.getActive() != null)
            return entityRepository.findAllByAnoAndIsActive(filter.getAno(), filter.getActive())
                    .stream().map(this::toDomain).toList();
        if (filter.getAno() != null)
            return entityRepository.findAllByAno(filter.getAno()).stream().map(this::toDomain).toList();
        if (filter.getIsNational() != null && filter.getActive() != null)
            return entityRepository.findAllByIsNationalAndIsActive(filter.getIsNational(), filter.getActive())
                    .stream().map(this::toDomain).toList();
        if (filter.getIsNational() != null)
            return entityRepository.findAllByIsNational(filter.getIsNational()).stream().map(this::toDomain).toList();
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(this::toDomain).toList();
        return entityRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsNacionalActivoByData(LocalDate data) {
        return entityRepository.existsByHolidayDateAndIsNationalTrueAndIsActiveTrue(data);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LocalDate> findAllNacionaisActivosByAno(int ano) {
        return entityRepository.findAllByIsNationalTrueAndIsActiveTrue().stream()
                .filter(e -> e.getHolidayDate().getYear() == ano)
                .map(PublicHolidayEntity::getHolidayDate)
                .toList();
    }
}
