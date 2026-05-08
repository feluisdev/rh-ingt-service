/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_budget_drivers")
public class CostDriverEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;


    @NotBlank(message = "driverType is mandatory")
    @Column(name="driver_type", nullable = false)
    private String driverType;


    @NotBlank(message = "params is mandatory")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="params", nullable = false, columnDefinition = "jsonb")
    private String params;


    @NotNull(message = "validFrom is mandatory")
    @Column(name="valid_from", nullable = false)
    private LocalDate validFrom;


    @NotBlank(message = "currency is mandatory")
    @Column(name="currency", nullable = false)
    private String currency = "CVE";


    @Column(name="legal_reference")
    private String legalReference;


}
