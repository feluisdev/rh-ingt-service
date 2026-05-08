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
@Entity(name = "ColabsContratoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_contrato")
public class ContratoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "contract_type_id", nullable = false)
    private UUID contractTypeId;

    @Column(name = "contract_number", unique = true, length = 100)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "termination_reason", length = 50)
    private String terminationReason;

    @Column(name = "is_current", nullable = false, columnDefinition = "boolean default false")
    private Boolean isCurrent;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount;

    @Column(name = "legal_base", length = 200)
    private String legalBase;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
