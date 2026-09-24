package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Historico do horario base: desde quando cada horario e o base da instituicao ({@code desde} nulo =
 * desde sempre). Tabela nova, criada pelo ddl-auto -- sem migracao.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_horario_base")
public class HorarioBaseEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "horario_id", nullable = false)
    private UUID horarioId;

    @Column(name = "desde")
    private LocalDate desde;
}
