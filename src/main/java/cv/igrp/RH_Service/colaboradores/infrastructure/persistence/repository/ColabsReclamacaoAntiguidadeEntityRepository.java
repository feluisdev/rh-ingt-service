package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReclamacaoAntiguidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsReclamacaoAntiguidadeEntityRepository extends JpaRepository<ReclamacaoAntiguidadeEntity, UUID> {

    @Query("""
            SELECT r FROM ColabsReclamacaoAntiguidadeEntity r
            WHERE r.lista.id = :listaId
            ORDER BY r.dataApresentacao, r.createdDate""")
    List<ReclamacaoAntiguidadeEntity> findDaLista(@Param("listaId") UUID listaId);
}
