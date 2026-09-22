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
@Table(name = "t_leave_type")
public class LeaveTypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "deducts_balance")
    private Boolean deductsBalance;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    /**
     * Limite por ACONTECIMENTO (V53). O art. 15.o n.o 1 do DL n.o 3/2010 quase so fala assim:
     * 6 dias por ocasiao do casamento, 8 por falecimento do conjuge, 2 por cada prova.
     * Nulo quer dizer que a lei nao poe limite desta natureza.
     */
    @Column(name = "max_days_per_occurrence")
    private Integer maxDaysPerOccurrence;

    /** Limite por mes civil (V53): art. 15.o n.o 1 al. o) e al. q). Nulo e sem limite. */
    @Column(name = "max_days_per_month")
    private Integer maxDaysPerMonth;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_active")
    private Boolean isActive;

    /** Regime legal do DL n.o 3/2010: FERIAS (cap. II) ou FALTA (cap. III). Ver V49. */
    @Column(name = "regime", length = 20)
    private String regime;
}
