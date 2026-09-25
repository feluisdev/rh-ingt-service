package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PedidoDeclaracaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsPedidoDeclaracaoEntityRepository extends JpaRepository<PedidoDeclaracaoEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsPedidoDeclaracaoEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.dataPedido DESC, p.createdDate DESC""")
    List<PedidoDeclaracaoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT p FROM ColabsPedidoDeclaracaoEntity p
            WHERE p.estado = 'PEDIDA'
            ORDER BY p.dataPedido, p.createdDate""")
    List<PedidoDeclaracaoEntity> findPorEmitir();
}
