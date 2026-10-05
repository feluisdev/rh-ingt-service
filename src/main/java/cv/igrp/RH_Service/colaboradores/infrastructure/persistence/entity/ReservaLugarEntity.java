package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Reserva de um Lugar para quem ainda não tem contrato. Tabela nova, criada pelo ddl-auto — sem migração, e sem
 * tocar em {@code t_assignment}.
 *
 * <p>Uma reserva activa por Lugar e uma por colaborador, garantidas pela base: {@code lugar_activo} e
 * {@code funcionario_activo} repetem o Lugar e o colaborador <b>só enquanto a reserva está activa</b> e ficam a
 * nulo quando fecha. Os índices únicos sobre elas fazem de índice parcial (no PostgreSQL os nulos não colidem),
 * que o JPA não sabe declarar.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsReservaLugarEntity")
@NoArgsConstructor
@Table(name = "t_reserva_lugar", indexes = {
        @Index(name = "ux_reserva_lugar_lugar_activo", columnList = "lugar_activo", unique = true),
        @Index(name = "ux_reserva_lugar_funcionario_activo", columnList = "funcionario_activo", unique = true),
        @Index(name = "ix_reserva_lugar_funcionario", columnList = "funcionario_id")
})
public class ReservaLugarEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "function_id")
    private UUID functionId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** ACTIVA, CONCLUIDA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @Column(name = "reservada_em", nullable = false)
    private LocalDate reservadaEm;

    @Column(name = "fechada_em")
    private LocalDate fechadaEm;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    /** O Lugar, só enquanto a reserva está activa. */
    @Column(name = "lugar_activo")
    private UUID lugarActivo;

    /** O colaborador, só enquanto a reserva está activa. */
    @Column(name = "funcionario_activo")
    private UUID funcionarioActivo;
}
