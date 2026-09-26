package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.repository;

import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.ConcursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecrutConcursoEntityRepository extends JpaRepository<ConcursoEntity, UUID> {

    @Query("""
            SELECT c FROM RecrutConcursoEntity c
            WHERE (:estado IS NULL OR c.estado = :estado)
            ORDER BY c.createdDate DESC""")
    List<ConcursoEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT COUNT(c) > 0 FROM RecrutConcursoEntity c
            WHERE upper(c.referencia) = upper(:referencia) AND (:excepto IS NULL OR c.id <> :excepto)""")
    boolean existeReferencia(@Param("referencia") String referencia, @Param("excepto") UUID excepto);

    @Query("""
            SELECT COUNT(c) > 0 FROM RecrutConcursoEntity c JOIN c.lugares l
            WHERE l = :lugarId AND c.estado NOT IN ('CONCLUIDO', 'ANULADO') AND (:excepto IS NULL OR c.id <> :excepto)""")
    boolean lugarEmConcursoActivo(@Param("lugarId") UUID lugarId, @Param("excepto") UUID excepto);
}
