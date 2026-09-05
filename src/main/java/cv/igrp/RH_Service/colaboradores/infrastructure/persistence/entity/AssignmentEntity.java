package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
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
@Entity(name = "ColabsAssignmentEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_assignment")
public class AssignmentEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private PositionEntity position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private GradeEntity grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private FunctionEntity function;

    @Column(name = "assignment_type", nullable = false, length = 20)
    private String assignmentType;

    @Column(name = "origem", nullable = false, length = 20)
    private String origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_assignment_id")
    private AssignmentEntity originAssignment;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "notes")
    private String notes;
}
