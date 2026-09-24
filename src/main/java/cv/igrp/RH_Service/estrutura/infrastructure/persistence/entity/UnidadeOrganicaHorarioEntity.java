package cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Historico do horario de cada unidade organica (horario nulo = segue a unidade-mae; desde nulo = desde
 * sempre). Sem FK, como a coluna horario_id da V57. Tabela nova, criada pelo ddl-auto -- sem migracao.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_unidade_organica_horario",
        indexes = @Index(name = "ix_unidade_horario_unidade", columnList = "unidade_id, desde"))
public class UnidadeOrganicaHorarioEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "horario_id")
    private UUID horarioId;

    @Column(name = "desde")
    private LocalDate desde;
}
