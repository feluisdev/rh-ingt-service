package cv.igrp.RH_Service.formacao.infrastructure.persistence.repository;

import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.PlanoFormacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FormPlanoFormacaoEntityRepository extends JpaRepository<PlanoFormacaoEntity, UUID> {

    @Query("""
            SELECT p FROM FormPlanoFormacaoEntity p
            WHERE (:ano IS NULL OR p.ano = :ano)
            ORDER BY p.ano DESC, p.designacao""")
    List<PlanoFormacaoEntity> find(@Param("ano") Integer ano);
}
