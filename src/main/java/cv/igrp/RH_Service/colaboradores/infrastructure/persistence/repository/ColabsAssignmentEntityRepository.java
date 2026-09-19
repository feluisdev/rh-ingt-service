package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsAssignmentEntityRepository extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByFuncionario_IdAndIsCurrentTrueAndAssignmentType(UUID funcionarioId, String assignmentType);
    List<AssignmentEntity> findByFuncionario_IdAndIsCurrentTrue(UUID funcionarioId);
    List<AssignmentEntity> findByFuncionario_IdOrderByDataInicioDesc(UUID funcionarioId);
    // Por Lugar filtra-se sempre tambem pelo tipo: desde a V45 um Lugar pode ter mais do
    // que uma afectacao corrente (o titular e quem o substitui), e um Optional sobre
    // "corrente" passaria a rebentar com NonUniqueResultException.
    Optional<AssignmentEntity> findByPosition_IdAndIsCurrentTrueAndAssignmentType(UUID positionId, String assignmentType);
    boolean existsByPosition_IdAndIsCurrentTrueAndAssignmentType(UUID positionId, String assignmentType);

    // Substituicoes de um titular impedido (V46). O tipo entra no nome derivado para que a
    // consulta nao dependa de a coluna titular_assignment_id so aparecer em substituicoes.
    Optional<AssignmentEntity> findByTitularAssignment_IdAndIsCurrentTrueAndAssignmentType(
            UUID titularAssignmentId, String assignmentType);
    List<AssignmentEntity> findAllByTitularAssignment_IdAndIsCurrentTrueAndAssignmentType(
            UUID titularAssignmentId, String assignmentType);

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

    // Provimento em bloco: um unico SELECT para todos os Lugares de uma listagem, em vez de
    // um exists por linha (N+1). Devolve apenas os ids dos Lugares com titular; os restantes
    // estao vagos por ausencia. O tipo entra por parametro para nao escrever 'PRINCIPAL' em
    // JPQL, que e' onde o enum deixaria de ser verificado.
    // Ver nota acima sobre o nome JPA da entidade ("ColabsAssignmentEntity").
    @Query("SELECT p.id FROM ColabsAssignmentEntity a JOIN a.position p "
            + "WHERE a.isCurrent = true AND a.assignmentType = :assignmentType AND p.id IN :positionIds")
    List<UUID> findPositionIdsComTitular(@Param("positionIds") Collection<UUID> positionIds,
                                         @Param("assignmentType") String assignmentType);
}
