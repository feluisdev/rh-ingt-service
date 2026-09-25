package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoAposentacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsProcessoAposentacaoEntityRepository extends JpaRepository<ProcessoAposentacaoEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsProcessoAposentacaoEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.dataPedido DESC, p.createdDate DESC""")
    List<ProcessoAposentacaoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
