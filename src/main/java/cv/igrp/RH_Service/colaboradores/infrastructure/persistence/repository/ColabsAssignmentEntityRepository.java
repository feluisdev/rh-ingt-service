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

    Optional<AssignmentEntity> findByFuncionario_IdAndIsCurrentTrueAndAssignmentType(UUID funcionarioId, String assignmentType);
    List<AssignmentEntity> findByFuncionario_IdAndIsCurrentTrue(UUID funcionarioId);
    List<AssignmentEntity> findByFuncionario_IdOrderByDataInicioDesc(UUID funcionarioId);
    Optional<AssignmentEntity> findByPosition_IdAndIsCurrentTrue(UUID positionId);
    boolean existsByPosition_IdAndIsCurrentTrue(UUID positionId);

    // Predicado temporal de sobreposicao de intervalo: uma afectacao cobre o intervalo
    // [startOfYear, endOfYear] se comecar antes ou no fim do intervalo (dataInicio <= endOfYear)
    // e terminar depois ou no inicio do intervalo, ou nunca terminar (dataFim IS NULL OR
    // dataFim >= startOfYear). O OR sobre a coluna anulavel dataFim nao se exprime em nome
    // derivado -- ver precedente em PaaSubmissionPeriodEntityRepository.
    //
    // A unidade organica vive no Lugar, nao na afectacao: o join percorre a associacao
    // a.position. O acesso a p.unidadeOrganica.id le a chave estrangeira do proprio
    // t_position, sem segundo join para t_unidade_organica.
    // O nome JPA da entidade e "ColabsAssignmentEntity", nao "AssignmentEntity": esta
    // fixado em @Entity(name=...) para nao colidir com outra Assignment no contexto de
    // persistencia. Em JPQL vale o nome da entidade, nao o da classe.
    @Query("SELECT a FROM ColabsAssignmentEntity a JOIN a.position p "
            + "WHERE p.unidadeOrganica.id = :unidadeOrganicaId "
            + "AND a.dataInicio <= :endOfYear "
            + "AND (a.dataFim IS NULL OR a.dataFim >= :startOfYear) "
            + "ORDER BY a.dataInicio ASC")
    List<AssignmentEntity> findAllByUnidadeOrganicaCoveringRange(
            @Param("unidadeOrganicaId") UUID unidadeOrganicaId,
            @Param("startOfYear") LocalDate startOfYear,
            @Param("endOfYear") LocalDate endOfYear);
}
