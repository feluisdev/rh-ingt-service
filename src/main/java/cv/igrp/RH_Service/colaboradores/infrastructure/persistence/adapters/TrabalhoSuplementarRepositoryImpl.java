package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoTrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.models.TrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.TrabalhoSuplementarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.TrabalhoSuplementarEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsTrabalhoSuplementarEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrabalhoSuplementarRepositoryImpl implements TrabalhoSuplementarRepository {

    private final ColabsTrabalhoSuplementarEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public TrabalhoSuplementar save(TrabalhoSuplementar t) {
        TrabalhoSuplementarEntity e = entityRepository.findById(t.getId().getValor()).orElseGet(() -> {
            TrabalhoSuplementarEntity novo = new TrabalhoSuplementarEntity();
            novo.setId(t.getId().getValor());
            novo.setFuncionario(refs.ref(FuncionarioEntity.class, t.getFuncionarioId().getValor()));
            novo.setData(t.getData());
            novo.setHoraInicio(t.getHoraInicio());
            novo.setHoraFim(t.getHoraFim());
            novo.setMotivo(t.getMotivo());
            novo.setPedidoPeloProprio(t.isPedidoPeloProprio());
            return novo;
        });
        // O intervalo não muda: só o estado e a decisão.
        e.setEstado(t.getEstado().name());
        e.setAutorizacaoPosterior(t.isAutorizacaoPosterior());
        e.setDecididoPor(t.getDecididoPor() != null ? t.getDecididoPor().getValor() : null);
        e.setDecididoEm(t.getDecididoEm());
        e.setMotivoRecusa(t.getMotivoRecusa());
        e.setMotivoCancelamento(t.getMotivoCancelamento());
        e.setCanceladoEm(t.getCanceladoEm());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TrabalhoSuplementar> findById(TrabalhoSuplementarId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrabalhoSuplementar> findByFuncionarioEntre(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        return entityRepository.findEntre(funcionarioId.getValor(), de, ate).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TrabalhoSuplementar> findPedidosDe(Collection<FuncionarioId> funcionarios) {
        if (funcionarios.isEmpty()) return List.of();
        return entityRepository.findPedidosDe(funcionarios.stream().map(FuncionarioId::getValor).toList())
                .stream().map(this::toDomain).toList();
    }

    private TrabalhoSuplementar toDomain(TrabalhoSuplementarEntity e) {
        return TrabalhoSuplementar.reconstruir(
                TrabalhoSuplementarId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                e.getData(), e.getHoraInicio(), e.getHoraFim(), e.getMotivo(),
                EstadoTrabalhoSuplementar.valueOf(e.getEstado()),
                Boolean.TRUE.equals(e.getPedidoPeloProprio()),
                Boolean.TRUE.equals(e.getAutorizacaoPosterior()),
                e.getDecididoPor() != null ? FuncionarioId.from(e.getDecididoPor()) : null,
                e.getDecididoEm(), e.getMotivoRecusa(), e.getMotivoCancelamento(), e.getCanceladoEm());
    }
}
