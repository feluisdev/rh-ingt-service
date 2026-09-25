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
 * Notificação na aplicação — para uma pessoa ({@code destinatario_id}) ou para a caixa partilhada de um
 * perfil ({@code perfil}), nunca as duas (invariante do domínio). O destinatário não é chave estrangeira:
 * o módulo {@code shared} não se amarra às tabelas dos colaboradores, e a notificação sobrevive.
 *
 * <p>Não é auditada pelo Envers: é ela própria um registo, e só muda uma vez (quando é lida).
 * Tabela nova, sem migração: nasce pelo {@code ddl-auto}.
 */
@Getter
@Setter
@IgrpEntity
@Entity(name = "SharedNotificacaoEntity")
@NoArgsConstructor
@Table(name = "t_notificacao", indexes = {
        @Index(name = "ix_notificacao_destinatario", columnList = "destinatario_id, criada_em"),
        @Index(name = "ix_notificacao_perfil", columnList = "perfil, criada_em")
})
public class NotificacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "destinatario_id")
    private UUID destinatarioId;

    /** Caixa partilhada (ex.: RH); nulo quando é para uma pessoa. */
    @Column(name = "perfil", length = 30)
    private String perfil;

    @Column(name = "tipo", nullable = false, length = 60)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "texto", length = 2000)
    private String texto;

    @Column(name = "recurso_tipo", length = 60)
    private String recursoTipo;

    @Column(name = "recurso_id", length = 60)
    private String recursoId;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;
}
