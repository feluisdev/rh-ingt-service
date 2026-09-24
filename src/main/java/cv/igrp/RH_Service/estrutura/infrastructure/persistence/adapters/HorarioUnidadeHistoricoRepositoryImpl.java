package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.estrutura.domain.models.VigenciaHorarioUnidade;
import cv.igrp.RH_Service.estrutura.domain.repository.HorarioUnidadeHistoricoRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.UnidadeOrganicaHorarioEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.UnidadeOrganicaHorarioEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HorarioUnidadeHistoricoRepositoryImpl implements HorarioUnidadeHistoricoRepository {

    private final UnidadeOrganicaHorarioEntityRepository entityRepository;

    @Transactional(readOnly = true)
    @Override
    public List<VigenciaHorarioUnidade> findByUnidade(OrganizationalUnitId unidadeId) {
        return entityRepository.findAllByUnidadeId(unidadeId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<VigenciaHorarioUnidade> findByHorario(HorarioId horarioId) {
        return entityRepository.findAllByHorarioId(horarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public void registar(VigenciaHorarioUnidade v) {
        if (v.desde() != null) {
            entityRepository.deleteAll(entityRepository.findAllByUnidadeIdAndDesde(v.unidadeId().getValor(), v.desde()));
            entityRepository.flush();
        }
        var e = new UnidadeOrganicaHorarioEntity();
        e.setId(UUID.randomUUID());
        e.setUnidadeId(v.unidadeId().getValor());
        e.setHorarioId(v.horarioId() != null ? v.horarioId().getValor() : null);
        e.setDesde(v.desde());
        entityRepository.save(e);
    }

    private VigenciaHorarioUnidade toDomain(UnidadeOrganicaHorarioEntity e) {
        return new VigenciaHorarioUnidade(OrganizationalUnitId.from(e.getUnidadeId()),
                e.getHorarioId() != null ? HorarioId.from(e.getHorarioId()) : null, e.getDesde());
    }
}
