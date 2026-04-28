package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_strategic_indicators")
public class StrategicIndicatorEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @NotBlank(message = "title is mandatory")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "formula")
    private String formula;

    @Column(name = "target")
    private BigDecimal target;

    @Lob
    @Column(name = "evaluation_criteria", columnDefinition = "TEXT")
    private String evaluationCriteria;

    @Column(name = "info_source")
    private String infoSource;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "criteria_superado")
    private String criteriaSuperado;

    @Column(name = "criteria_seguranca")
    private String criteriaSeguranca;

    @Column(name = "criteria_alcancado")
    private String criteriaAlcancado;

    @Column(name = "criteria_insuficiente")
    private String criteriaInsuficiente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private StrategicGoalEntity goal;
}
