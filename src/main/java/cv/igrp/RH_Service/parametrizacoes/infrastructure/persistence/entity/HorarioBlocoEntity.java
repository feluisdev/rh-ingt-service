package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalTime;
import java.util.UUID;

/** Um bloco de um dia da semana (1 = segunda ... 7 = domingo, ISO-8601). */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_horario_bloco")
public class HorarioBlocoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioEntity horario;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    /** Plataforma fixa, no flexível. No fixo é sempre verdadeiro. */
    @Column(name = "obrigatorio", nullable = false)
    private Boolean obrigatorio;
}
