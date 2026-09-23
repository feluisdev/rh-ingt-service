package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.TrabalhoSuplementarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsTrabalhoSuplementarEntityRepository extends JpaRepository<TrabalhoSuplementarEntity, UUID> {

    @Query("""
            SELECT t FROM ColabsTrabalhoSuplementarEntity t
            WHERE t.funcionario.id = :funcionarioId AND t.data >= :de AND t.data <= :ate
            ORDER BY t.data, t.horaInicio""")
    List<TrabalhoSuplementarEntity> findEntre(@Param("funcionarioId") UUID funcionarioId,
                                              @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    /** Os pedidos por decidir destes colaboradores, do dia mais próximo para o mais distante. */
    @Query("""
            SELECT t FROM ColabsTrabalhoSuplementarEntity t
            WHERE t.funcionario.id IN :funcionarios AND t.estado = 'PEDIDO'
            ORDER BY t.data, t.horaInicio""")
    List<TrabalhoSuplementarEntity> findPedidosDe(@Param("funcionarios") List<UUID> funcionarios);
}
