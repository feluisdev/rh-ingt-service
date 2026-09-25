package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ListaAntiguidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsListaAntiguidadeEntityRepository extends JpaRepository<ListaAntiguidadeEntity, UUID> {

    @Query("""
            SELECT l FROM ColabsListaAntiguidadeEntity l
            WHERE (:ano IS NULL OR l.ano = :ano) AND (:unidadeId IS NULL OR l.unidadeId = :unidadeId)
            ORDER BY l.ano DESC, l.dataAprovacao DESC""")
    List<ListaAntiguidadeEntity> find(@Param("ano") Integer ano, @Param("unidadeId") UUID unidadeId);

    @Query("""
            SELECT COUNT(l) > 0 FROM ColabsListaAntiguidadeEntity l
            WHERE l.ano = :ano AND l.unidadeId = :unidadeId AND l.estado <> 'ANULADA'""")
    boolean existeActiva(@Param("ano") int ano, @Param("unidadeId") UUID unidadeId);

    @Query("""
            SELECT DISTINCT l FROM ColabsListaAntiguidadeEntity l JOIN l.linhas x
            WHERE x.funcionarioId = :funcionarioId AND l.estado IN ('AFIXADA', 'DEFINITIVA', 'PUBLICADA')
            ORDER BY l.ano DESC""")
    List<ListaAntiguidadeEntity> findVisiveisPara(@Param("funcionarioId") UUID funcionarioId);
}
