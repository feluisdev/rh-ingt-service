/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */
package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_mobility_subtype")
public class LeaveMobilitySubtypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "record_type", length = 20)
    private String recordType;

    @Column(name = "affects_pay")
    private Boolean affectsPay;

    @Column(name = "counts_for_seniority")
    private Boolean countsForSeniority;

    @Column(name = "can_self_submit")
    private Boolean canSelfSubmit;

    @Column(name = "is_active")
    private Boolean isActive;

    /** Duração máxima em dias (mobilidade transitória: 365) — ver V41. */
    @Column(name = "max_duration_days")
    private Integer maxDurationDays;

    /** Prorrogações permitidas (em regra, uma) — ver V41. */
    @Column(name = "max_extensions")
    private Integer maxExtensions;

    /** MANTEM | ABRE_VAGA — o que faz ao Lugar enquanto dura (ver V43). */
    @Column(name = "position_effect", length = 20)
    private String positionEffect;

    /** Abre vaga só além deste número de dias; nulo = abre logo (ver V43). */
    @Column(name = "vacancy_after_days")
    private Integer vacancyAfterDays;

    /** REGRESSA_LUGAR | DISPONIBILIDADE — o que acontece no regresso (ver V43). */
    @Column(name = "return_effect", length = 20)
    private String returnEffect;

    /**
     * Rótulo do subtipo. A V6 declara a coluna NOT NULL e a entity não a mapeava,
     * obrigando todo o INSERT a preenchê-la à mão fora da aplicação.
     */
    @Column(name = "name")
    private String name;
}
