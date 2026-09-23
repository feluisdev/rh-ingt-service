package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MarcacaoAssiduidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ColabsMarcacaoAssiduidadeEntityRepository extends JpaRepository<MarcacaoAssiduidadeEntity, UUID> {

    /** [{@code desde}, {@code antes}[ — o fim é exclusivo, para apanhar o último dia inteiro. */
    @Query("""
            SELECT m FROM ColabsMarcacaoAssiduidadeEntity m
            WHERE m.funcionario.id = :funcionarioId AND m.momento >= :desde AND m.momento < :antes
            ORDER BY m.momento""")
    List<MarcacaoAssiduidadeEntity> findEntre(@Param("funcionarioId") UUID funcionarioId,
                                              @Param("desde") LocalDateTime desde,
                                              @Param("antes") LocalDateTime antes);

    @Query("""
            SELECT count(m) > 0 FROM ColabsMarcacaoAssiduidadeEntity m
            WHERE m.funcionario.id = :funcionarioId AND m.anulada = false
              AND m.momento >= :desde AND m.momento < :antes""")
    boolean existeValidaEntre(@Param("funcionarioId") UUID funcionarioId,
                              @Param("desde") LocalDateTime desde,
                              @Param("antes") LocalDateTime antes);

    boolean existsByReferenciaExterna(String referenciaExterna);

    /** Os pedidos de correcção por decidir destes colaboradores, do mais antigo para o mais recente. */
    @Query("""
            SELECT m FROM ColabsMarcacaoAssiduidadeEntity m
            WHERE m.funcionario.id IN :funcionarios AND m.estado = 'PENDENTE' AND m.anulada = false
            ORDER BY m.momento""")
    List<MarcacaoAssiduidadeEntity> findPendentesDe(@Param("funcionarios") List<UUID> funcionarios);
}
