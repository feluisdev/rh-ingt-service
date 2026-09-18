package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PedidoAusenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsPedidoAusenciaEntityRepository extends JpaRepository<PedidoAusenciaEntity, UUID> {

    List<PedidoAusenciaEntity> findAllByFuncionario_Id(UUID funcionarioId);

    /**
     * Pedidos de um colaborador, com os filtros aplicados <b>na base de dados</b>.
     * Antes trazia-se tudo e filtrava-se em memória, o que cresce com o histórico.
     * O nome usado no JPQL é o da {@code @Entity}, não o da classe.
     */
    @Query("SELECT p FROM ColabsPedidoAusenciaEntity p " +
           "WHERE p.funcionario.id = :funcionarioId " +
           "AND (:estado IS NULL OR UPPER(p.estado) = UPPER(:estado)) " +
           "AND (:tipoAusenciaId IS NULL OR p.tipoAusencia.id = :tipoAusenciaId) " +
           "AND (:ano IS NULL OR YEAR(p.dataInicio) = :ano) " +
           "ORDER BY p.dataInicio DESC")
    List<PedidoAusenciaEntity> findAllByFuncionarioFiltrado(@Param("funcionarioId") UUID funcionarioId,
                                                            @Param("estado") String estado,
                                                            @Param("tipoAusenciaId") UUID tipoAusenciaId,
                                                            @Param("ano") Integer ano);

    /**
     * Dias já pedidos no ano para um tipo de ausência, ignorando os pedidos sem
     * efeito (rejeitados e cancelados). Somado na base de dados, para não trazer
     * o histórico todo só para contar.
     */
    @Query("SELECT COALESCE(SUM(p.numeroDias), 0) FROM ColabsPedidoAusenciaEntity p " +
           "WHERE p.funcionario.id = :funcionarioId " +
           "AND p.tipoAusencia.id = :tipoAusenciaId " +
           "AND YEAR(p.dataInicio) = :ano " +
           "AND p.estado NOT IN ('REJEITADO', 'CANCELADO')")
    int somarDiasNoAno(@Param("funcionarioId") UUID funcionarioId,
                       @Param("tipoAusenciaId") UUID tipoAusenciaId,
                       @Param("ano") int ano);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM ColabsPedidoAusenciaEntity p " +
           "WHERE p.funcionario.id = :funcionarioId " +
           "AND p.estado IN ('APROVADO', 'PENDENTE') " +
           "AND p.dataInicio <= :dataFim " +
           "AND p.dataFim >= :dataInicio")
    boolean existsOverlap(@Param("funcionarioId") UUID funcionarioId,
                          @Param("dataInicio") LocalDate dataInicio,
                          @Param("dataFim") LocalDate dataFim);
}
