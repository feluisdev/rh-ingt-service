package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.CartaoProfissionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsCartaoProfissionalEntityRepository extends JpaRepository<CartaoProfissionalEntity, UUID> {

    @Query("""
            SELECT c FROM ColabsCartaoProfissionalEntity c
            WHERE c.funcionario.id = :funcionarioId
            ORDER BY c.emitidoEm DESC, c.createdDate DESC""")
    List<CartaoProfissionalEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
