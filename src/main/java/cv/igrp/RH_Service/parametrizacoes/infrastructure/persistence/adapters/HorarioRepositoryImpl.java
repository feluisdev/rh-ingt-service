package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.HorarioBlocoEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.HorarioEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.HorarioEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HorarioRepositoryImpl implements HorarioRepository {

    private final HorarioEntityRepository entityRepository;

    @Transactional
    @Override
    public Horario save(Horario h) {
        HorarioEntity e = entityRepository.findById(h.getId().getValor()).orElseGet(() -> {
            HorarioEntity novo = new HorarioEntity();
            novo.setId(h.getId().getValor());
            return novo;
        });
        e.setNome(h.getNome());
        e.setControlo(h.getControlo().name());
        e.setPeriodoAfericao(h.getPeriodoAfericao() != null ? h.getPeriodoAfericao().name() : null);
        e.setDuracaoDiariaMinutos(h.getDuracaoDiariaMinutos());
        e.setIsBase(h.isBase());
        e.setIsActive(h.isActive());

        // Os blocos substituem-se por inteiro: o horário é o conjunto actual.
        e.getBlocos().clear();
        for (BlocoHorario b : h.getBlocos()) {
            HorarioBlocoEntity be = new HorarioBlocoEntity();
            be.setId(UUID.randomUUID());
            be.setHorario(e);
            be.setDiaSemana(b.dia().getValue());
            be.setHoraInicio(b.inicio());
            be.setHoraFim(b.fim());
            be.setObrigatorio(b.obrigatorio());
            e.getBlocos().add(be);
        }
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Horario> findById(HorarioId id) {
        return entityRepository.findById(id.getValor()).map(HorarioRepositoryImpl::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Horario> findAll(Boolean active) {
        var entidades = active == null
                ? entityRepository.findAllByOrderByNomeAsc()
                : entityRepository.findAllByIsActiveOrderByNomeAsc(active);
        return entidades.stream().map(HorarioRepositoryImpl::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Horario> findBase() {
        return entityRepository.findFirstByIsBaseTrue().map(HorarioRepositoryImpl::toDomain);
    }

    static Horario toDomain(HorarioEntity e) {
        List<BlocoHorario> blocos = e.getBlocos().stream()
                .map(b -> new BlocoHorario(DayOfWeek.of(b.getDiaSemana()), b.getHoraInicio(), b.getHoraFim(),
                        Boolean.TRUE.equals(b.getObrigatorio())))
                .toList();
        return Horario.reconstruir(
                HorarioId.from(e.getId()),
                e.getNome(),
                ControloHorario.valueOf(e.getControlo()),
                e.getPeriodoAfericao() != null ? PeriodoAfericao.valueOf(e.getPeriodoAfericao()) : null,
                e.getDuracaoDiariaMinutos(),
                blocos,
                Boolean.TRUE.equals(e.getIsBase()),
                Boolean.TRUE.equals(e.getIsActive()));
    }
}
