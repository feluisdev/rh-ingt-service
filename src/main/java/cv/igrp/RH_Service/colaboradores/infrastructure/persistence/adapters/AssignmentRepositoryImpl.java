package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.AssignmentMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsAssignmentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("colabsAssignmentRepositoryImpl")
@RequiredArgsConstructor
public class AssignmentRepositoryImpl implements AssignmentRepository {

    private final ColabsAssignmentEntityRepository entityRepository;
    private final AssignmentMapper mapper;

    @Transactional
    @Override
    public Assignment save(Assignment assignment) {
        // saveAndFlush: garante que o fecho da afectação corrente (is_current=false) é
        // persistido ANTES do insert da nova (is_current=true) — o Hibernate ordena
        // inserts antes de updates, o que violaria o índice único parcial por funcionário.
        return mapper.toDomain(entityRepository.saveAndFlush(mapper.toEntity(assignment)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Assignment> findById(AssignmentId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Assignment> findCurrentPrincipalByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository
                .findByFuncionarioIdAndIsCurrentTrueAndAssignmentType(funcionarioId.getValor(), Assignment.PRINCIPAL)
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findCurrentByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioIdAndIsCurrentTrue(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findAllByFuncionarioOrderByDataInicioDesc(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionarioIdOrderByDataInicioDesc(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Assignment> findCurrentByPosition(UUID positionId) {
        return entityRepository.findByPositionIdAndIsCurrentTrue(positionId).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isPositionOccupied(UUID positionId) {
        return entityRepository.existsByPositionIdAndIsCurrentTrue(positionId);
    }
}
