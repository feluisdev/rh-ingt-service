package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReservaLugarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReservaLugarEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsReservaLugarEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservaLugarRepositoryImpl implements ReservaLugarRepository {

    private final ColabsReservaLugarEntityRepository entityRepository;

    @Transactional
    @Override
    public ReservaLugar save(ReservaLugar r) {
        var e = entityRepository.findById(r.getId().getValor()).orElseGet(() -> {
            var n = new ReservaLugarEntity();
            n.setId(r.getId().getValor());
            n.setFuncionarioId(r.getFuncionarioId().getValor());
            n.setReservadaEm(r.getReservadaEm());
            return n;
        });
        e.setPositionId(r.getPositionId());
        e.setGradeId(r.getGradeId());
        e.setFunctionId(r.getFunctionId());
        e.setNotes(r.getNotes());
        e.setEstado(r.getEstado().name());
        e.setFechadaEm(r.getFechadaEm());
        e.setMotivo(r.getMotivo());
        e.setAssignmentId(r.getAssignmentId());
        e.setLugarActivo(r.activa() ? r.getPositionId() : null);
        e.setFuncionarioActivo(r.activa() ? r.getFuncionarioId().getValor() : null);
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReservaLugar> findActivaByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioActivo(funcionarioId.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReservaLugar> findActivaByPosition(UUID positionId) {
        return entityRepository.findByLugarActivo(positionId).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservaLugar> findActivasByPositions(Collection<UUID> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) return List.of();
        return entityRepository.findByLugarActivoIn(positionIds).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReservaLugar> findActivasReservadasEm(Collection<LocalDate> dias) {
        if (dias == null || dias.isEmpty()) return List.of();
        return entityRepository.findByEstadoAndReservadaEmIn(ReservaLugar.Estado.ACTIVA.name(), dias)
                .stream().map(this::toDomain).toList();
    }

    private ReservaLugar toDomain(ReservaLugarEntity e) {
        return ReservaLugar.reconstruir(ReservaLugarId.from(e.getId()), FuncionarioId.from(e.getFuncionarioId()),
                e.getPositionId(), e.getGradeId(), e.getFunctionId(), e.getNotes(),
                ReservaLugar.Estado.valueOf(e.getEstado()), e.getReservadaEm(), e.getFechadaEm(), e.getMotivo(),
                e.getAssignmentId());
    }
}
