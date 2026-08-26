package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnquadramentoEntityRepository extends JpaRepository<EnquadramentoEntity, UUID> {
    Optional<EnquadramentoEntity> findByFuncionarioIdAndIsCurrentTrue(UUID funcionarioId);
    List<EnquadramentoEntity> findByFuncionarioIdOrderByDataInicioDesc(UUID funcionarioId);

    // Predicado temporal de sobreposição de intervalo: um enquadramento cobre o intervalo
    // [startOfYear, endOfYear] se comecar antes ou no fim do intervalo (dataInicio <= endOfYear)
    // e terminar depois ou no inicio do intervalo, ou nunca terminar (dataFim IS NULL OR
    // dataFim >= startOfYear). O OR sobre a coluna anulavel dataFim nao se exprime em nome
    // derivado -- ver precedente em PaaSubmissionPeriodEntityRepository.
    @Query("SELECT e FROM EnquadramentoEntity e WHERE e.unidadeOrganicaId = :unidadeOrganicaId "
            + "AND e.dataInicio <= :endOfYear "
            + "AND (e.dataFim IS NULL OR e.dataFim >= :startOfYear) "
            + "ORDER BY e.dataInicio ASC")
    List<EnquadramentoEntity> findAllByUnidadeOrganicaIdCoveringRange(
            @Param("unidadeOrganicaId") UUID unidadeOrganicaId,
            @Param("startOfYear") LocalDate startOfYear,
            @Param("endOfYear") LocalDate endOfYear);
}
