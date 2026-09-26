package cv.igrp.RH_Service.shared.infrastructure.persistence.repository.notificacoes;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.notificacoes.NotificacaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificacaoEntityRepository extends JpaRepository<NotificacaoEntity, UUID> {

    @Query("""
            SELECT n FROM SharedNotificacaoEntity n
            WHERE n.destinatarioId = :destinatario AND (:soNaoLidas = false OR n.lidaEm IS NULL)
            ORDER BY n.criadaEm DESC""")
    Page<NotificacaoEntity> findDoDestinatario(@Param("destinatario") UUID destinatario,
                                               @Param("soNaoLidas") boolean soNaoLidas, Pageable pageable);

    @Query("""
            SELECT n FROM SharedNotificacaoEntity n
            WHERE n.perfil = :perfil AND (:soNaoLidas = false OR n.lidaEm IS NULL)
            ORDER BY n.criadaEm DESC""")
    Page<NotificacaoEntity> findDoPerfil(@Param("perfil") String perfil,
                                         @Param("soNaoLidas") boolean soNaoLidas, Pageable pageable);

    long countByDestinatarioIdAndLidaEmIsNull(UUID destinatarioId);

    long countByPerfilAndLidaEmIsNull(String perfil);

    @Modifying
    @Query("""
            UPDATE SharedNotificacaoEntity n SET n.lidaEm = :agora
            WHERE n.destinatarioId = :destinatario AND n.lidaEm IS NULL""")
    int marcarTodasLidas(@Param("destinatario") UUID destinatario, @Param("agora") LocalDateTime agora);

    boolean existsByTipoAndRecursoTipoAndRecursoId(String tipo, String recursoTipo, String recursoId);
}
