package cv.igrp.RH_Service.shared.infrastructure.persistence.repository.notificacoes;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.notificacoes.NotificacaoEnvioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacaoEnvioEntityRepository extends JpaRepository<NotificacaoEnvioEntity, UUID> {

    /** As que esperam envio, das mais antigas para as mais recentes. */
    List<NotificacaoEnvioEntity> findByEstadoOrderByCriadoEmAsc(String estado, Pageable pageable);

    long countByEstado(String estado);
}
