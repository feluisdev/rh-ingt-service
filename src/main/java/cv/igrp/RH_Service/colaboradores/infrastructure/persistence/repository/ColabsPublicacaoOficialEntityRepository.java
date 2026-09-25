package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PublicacaoOficialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsPublicacaoOficialEntityRepository extends JpaRepository<PublicacaoOficialEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsPublicacaoOficialEntity p
            WHERE (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.dataActo, p.createdDate""")
    List<PublicacaoOficialEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT p FROM ColabsPublicacaoOficialEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.dataActo DESC""")
    List<PublicacaoOficialEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT COUNT(p) > 0 FROM ColabsPublicacaoOficialEntity p
            WHERE p.referenciaTipo = :tipo AND p.referenciaId = :id AND p.estado <> 'CANCELADA'""")
    boolean existeParaReferencia(@Param("tipo") String tipo, @Param("id") String id);
}
