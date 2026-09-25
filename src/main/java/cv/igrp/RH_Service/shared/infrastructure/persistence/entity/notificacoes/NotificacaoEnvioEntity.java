package cv.igrp.RH_Service.shared.infrastructure.persistence.entity.notificacoes;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fila de envio de uma notificação por um canal externo (hoje só EMAIL). O envio é {@code TODO(smtp)}:
 * as linhas ficam PENDENTE até haver canal; o job {@code RH_ENVIO_NOTIFICACOES} (desactivado por
 * omissão) é quem as há-de despachar.
 *
 * <p>Tabela nova, sem migração: nasce pelo {@code ddl-auto}.
 */
@Getter
@Setter
@IgrpEntity
@Entity(name = "SharedNotificacaoEnvioEntity")
@NoArgsConstructor
@Table(name = "t_notificacao_envio", indexes = {
        @Index(name = "ix_notificacao_envio_estado", columnList = "estado, criado_em")
})
public class NotificacaoEnvioEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "notificacao_id", nullable = false)
    private UUID notificacaoId;

    /** EMAIL. */
    @Column(name = "canal", nullable = false, length = 20)
    private String canal;

    /** PENDENTE, ENVIADO ou FALHOU. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;
}
