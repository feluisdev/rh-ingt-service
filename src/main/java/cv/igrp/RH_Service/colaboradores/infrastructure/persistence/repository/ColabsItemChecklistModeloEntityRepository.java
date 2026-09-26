package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ItemChecklistModeloEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsItemChecklistModeloEntityRepository extends JpaRepository<ItemChecklistModeloEntity, UUID> {

    @Query("""
            SELECT m FROM ColabsItemChecklistModeloEntity m
            WHERE (:tipo IS NULL OR m.tipo = :tipo)
            ORDER BY m.tipo, m.ordem, m.descricao""")
    List<ItemChecklistModeloEntity> find(@Param("tipo") String tipo);

    @Query("""
            SELECT COUNT(m) > 0 FROM ColabsItemChecklistModeloEntity m
            WHERE m.tipo = :tipo AND m.codigo = :codigo AND (:excepto IS NULL OR m.id <> :excepto)""")
    boolean existeCodigo(@Param("tipo") String tipo, @Param("codigo") String codigo, @Param("excepto") UUID excepto);
}
