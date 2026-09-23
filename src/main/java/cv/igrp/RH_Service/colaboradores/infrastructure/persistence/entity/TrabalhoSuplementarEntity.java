package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Trabalho suplementar (Lei n.o 20/X/2023, art. 155.o n.o 2 a)): a autorizacao de um intervalo de um
 * dia. As horas realizadas calculam-se das marcacoes, nao se guardam.
 * Tabela nova, criada pelo ddl-auto -- sem migracao.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsTrabalhoSuplementarEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_trabalho_suplementar",
        indexes = @Index(name = "ix_trabalho_suplementar_funcionario_data", columnList = "funcionario_id, data"))
public class TrabalhoSuplementarEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    /** PEDIDO, AUTORIZADO, RECUSADO ou CANCELADO. */
    @Column(name = "estado", nullable = false, length = 12)
    private String estado;

    @Column(name = "pedido_pelo_proprio", nullable = false)
    private Boolean pedidoPeloProprio;

    @Column(name = "autorizacao_posterior", nullable = false)
    private Boolean autorizacaoPosterior;

    /** A chefia directa que decidiu; nulo quando foi o RH. */
    @Column(name = "decidido_por")
    private UUID decididoPor;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;
}
