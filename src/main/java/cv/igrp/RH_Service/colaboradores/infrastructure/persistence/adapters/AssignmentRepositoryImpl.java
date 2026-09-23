package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.AssignmentMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsAssignmentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                .findByFuncionario_IdAndIsCurrentTrueAndAssignmentType(
                        funcionarioId.getValor(), TipoAfectacao.PRINCIPAL.name())
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findCurrentByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario_IdAndIsCurrentTrue(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findAllByFuncionarioOrderByDataInicioDesc(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario_IdOrderByDataInicioDesc(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Assignment> findTitularByPosition(UUID positionId) {
        return entityRepository
                .findByPosition_IdAndIsCurrentTrueAndAssignmentType(positionId, TipoAfectacao.PRINCIPAL.name())
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean temTitular(UUID positionId) {
        return entityRepository
                .existsByPosition_IdAndIsCurrentTrueAndAssignmentType(positionId, TipoAfectacao.PRINCIPAL.name());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Assignment> findSubstitutoCorrente(AssignmentId titularAssignmentId) {
        return entityRepository
                .findByTitularAssignment_IdAndIsCurrentTrueAndAssignmentType(
                        titularAssignmentId.getValor(), TipoAfectacao.SUBSTITUICAO.name())
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findSubstituicoesCorrentes(AssignmentId titularAssignmentId) {
        return entityRepository
                .findAllByTitularAssignment_IdAndIsCurrentTrueAndAssignmentType(
                        titularAssignmentId.getValor(), TipoAfectacao.SUBSTITUICAO.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findSubstituicoesDoFuncionario(FuncionarioId funcionarioId, boolean apenasCorrentes) {
        var linhas = apenasCorrentes
                ? entityRepository.findSubstituicoesCorrentesDoFuncionario(funcionarioId.getValor())
                : entityRepository.findSubstituicoesDoFuncionario(funcionarioId.getValor());
        return linhas.stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findSubstituicoesCorrentesDoLugar(java.util.UUID positionId) {
        return entityRepository.findSubstituicoesCorrentesDoLugar(positionId)
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Set<UUID> findPositionIdsComTitular(Collection<UUID> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) return Set.of();
        return Set.copyOf(entityRepository.findPositionIdsComTitular(positionIds, TipoAfectacao.PRINCIPAL.name()));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Assignment> findAllByUnidadeOrganicaCoveringYear(UUID unidadeOrganicaId, int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        return entityRepository.findAllByUnidadeOrganicaCoveringRange(unidadeOrganicaId, startOfYear, endOfYear)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findAllByUnidadeOrganicaEntre(UUID unidadeOrganicaId, LocalDate de, LocalDate ate) {
        return entityRepository.findAllByUnidadeOrganicaCoveringRange(unidadeOrganicaId, de, ate)
                .stream().map(mapper::toDomain).toList();
    }
}
