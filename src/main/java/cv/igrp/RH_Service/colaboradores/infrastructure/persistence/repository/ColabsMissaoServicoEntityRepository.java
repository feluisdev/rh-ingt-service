package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MissaoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ColabsMissaoServicoEntityRepository extends JpaRepository<MissaoServicoEntity, UUID> {

    @Query("""
            SELECT DISTINCT m FROM ColabsMissaoServicoEntity m LEFT JOIN m.participantes p
            WHERE (:estado IS NULL OR m.estado = :estado) AND (:participante IS NULL OR p = :participante)
            ORDER BY m.partida DESC""")
    List<MissaoServicoEntity> find(@Param("estado") String estado, @Param("participante") UUID participante);

    @Query("""
            SELECT DISTINCT m FROM ColabsMissaoServicoEntity m JOIN m.participantes p
            WHERE p = :participante AND m.estado IN ('PEDIDA', 'AUTORIZADA')
              AND m.partida < :ate AND m.regresso > :de AND m.id <> :excepto""")
    List<MissaoServicoEntity> findSobrepostas(@Param("participante") UUID participante, @Param("de") LocalDateTime de,
                                              @Param("ate") LocalDateTime ate, @Param("excepto") UUID excepto);

    @Query("""
            SELECT DISTINCT m FROM ColabsMissaoServicoEntity m JOIN m.participantes p
            WHERE p = :participante AND m.estado IN ('AUTORIZADA', 'REALIZADA')
              AND m.partida < :ate AND m.regresso >= :de""")
    List<MissaoServicoEntity> findQueContamEntre(@Param("participante") UUID participante, @Param("de") LocalDateTime de,
                                                 @Param("ate") LocalDateTime ate);

    @Query("""
            SELECT m FROM ColabsMissaoServicoEntity m
            WHERE m.estado = 'AUTORIZADA' AND m.regresso >= :de AND m.regresso < :ate""")
    List<MissaoServicoEntity> findAutorizadasComRegressoEntre(@Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate);
}
