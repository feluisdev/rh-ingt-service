package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.models.VigenciaHorarioBase;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioBaseHistoricoRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.HorarioBaseEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.HorarioBaseEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HorarioBaseHistoricoRepositoryImpl implements HorarioBaseHistoricoRepository {

    private final HorarioBaseEntityRepository entityRepository;

    @Transactional(readOnly = true)
    @Override
    public List<VigenciaHorarioBase> findAll() {
        return entityRepository.findAll().stream()
                .map(e -> new VigenciaHorarioBase(HorarioId.from(e.getHorarioId()), e.getDesde())).toList();
    }

    @Transactional
    @Override
    public void registar(VigenciaHorarioBase v) {
        if (v.desde() != null) apagarDesde(v.desde());
        var e = new HorarioBaseEntity();
        e.setId(UUID.randomUUID());
        e.setHorarioId(v.horarioId().getValor());
        e.setDesde(v.desde());
        entityRepository.save(e);
    }

    @Transactional
    @Override
    public void apagarDesde(LocalDate desde) {
        entityRepository.deleteAll(entityRepository.findAllByDesde(desde));
        entityRepository.flush();
    }
}
