package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsAssignmentEntityRepository extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByFuncionarioIdAndIsCurrentTrueAndAssignmentType(UUID funcionarioId, String assignmentType);
    List<AssignmentEntity> findByFuncionarioIdAndIsCurrentTrue(UUID funcionarioId);
    List<AssignmentEntity> findByFuncionarioIdOrderByDataInicioDesc(UUID funcionarioId);
    Optional<AssignmentEntity> findByPositionIdAndIsCurrentTrue(UUID positionId);
    boolean existsByPositionIdAndIsCurrentTrue(UUID positionId);

    // Predicado temporal de sobreposicao de intervalo: uma afectacao cobre o intervalo
    // [startOfYear, endOfYear] se comecar antes ou no fim do intervalo (dataInicio <= endOfYear)
    // e terminar depois ou no inicio do intervalo, ou nunca terminar (dataFim IS NULL OR
    // dataFim >= startOfYear). O OR sobre a coluna anulavel dataFim nao se exprime em nome
    // derivado -- ver precedente em PaaSubmissionPeriodEntityRepository.
    //
    // A unidade organica vive no Lugar, nao na afectacao: o join e explicito sobre
    // PositionEntity porque AssignmentEntity guarda positionId como UUID simples (sem
    // @ManyToOne), decisao do modelo de Position Management para manter os agregados
    // desacoplados. Em JPQL o join sem associacao faz-se por igualdade na clausula WHERE.
    @Query("SELECT a FROM AssignmentEntity a, PositionEntity p "
            + "WHERE p.id = a.positionId "
            + "AND p.unidadeOrganicaId = :unidadeOrganicaId "
            + "AND a.dataInicio <= :endOfYear "
            + "AND (a.dataFim IS NULL OR a.dataFim >= :startOfYear) "
            + "ORDER BY a.dataInicio ASC")
    List<AssignmentEntity> findAllByUnidadeOrganicaCoveringRange(
            @Param("unidadeOrganicaId") UUID unidadeOrganicaId,
            @Param("startOfYear") LocalDate startOfYear,
            @Param("endOfYear") LocalDate endOfYear);
}
