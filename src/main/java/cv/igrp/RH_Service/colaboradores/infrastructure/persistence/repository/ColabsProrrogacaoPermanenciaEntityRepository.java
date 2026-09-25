package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProrrogacaoPermanenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsProrrogacaoPermanenciaEntityRepository extends JpaRepository<ProrrogacaoPermanenciaEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsProrrogacaoPermanenciaEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.dataPedido DESC, p.createdDate DESC""")
    List<ProrrogacaoPermanenciaEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
