/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_employee_professional_assignments")
public class EnquadramentoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "career_id", nullable = false)
    private UUID careerId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "cargo_id", nullable = false)
    private UUID cargoId;

    @Column(name = "function_id")
    private UUID functionId;

    @Column(name = "unidade_organica_id", nullable = false)
    private UUID unidadeOrganicaId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;
}
