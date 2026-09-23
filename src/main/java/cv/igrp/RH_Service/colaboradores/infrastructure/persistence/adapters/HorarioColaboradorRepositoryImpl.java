package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.HorarioColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.HorarioColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HorarioColaboradorId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.HorarioColaboradorEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsHorarioColaboradorEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HorarioColaboradorRepositoryImpl implements HorarioColaboradorRepository {

    private final ColabsHorarioColaboradorEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public HorarioColaborador save(HorarioColaborador h) {
        HorarioColaboradorEntity e = entityRepository.findById(h.getId().getValor()).orElseGet(() -> {
            HorarioColaboradorEntity novo = new HorarioColaboradorEntity();
            novo.setId(h.getId().getValor());
            novo.setFuncionario(refs.ref(FuncionarioEntity.class, h.getFuncionarioId().getValor()));
            return novo;
        });
        e.setHorarioId(h.getHorarioId().getValor());
        e.setRegimePrestacao(h.getRegimePrestacao().name());
        e.setDataInicio(h.getDataInicio());
        e.setDataFim(h.getDataFim());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public List<HorarioColaborador> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioId(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<HorarioColaborador> findVigente(FuncionarioId funcionarioId, LocalDate data) {
        return entityRepository.findVigente(funcionarioId.getValor(), data).map(this::toDomain);
    }

    private HorarioColaborador toDomain(HorarioColaboradorEntity e) {
        return HorarioColaborador.reconstruir(
                HorarioColaboradorId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                HorarioId.from(e.getHorarioId()),
                RegimePrestacao.valueOf(e.getRegimePrestacao()),
                e.getDataInicio(),
                e.getDataFim());
    }
}
