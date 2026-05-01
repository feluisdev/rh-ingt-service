package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

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
@Entity(name = "ColabsSaldoAusenciaEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_balance",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_leave_balance_func_tipo_ano",
                columnNames = {"funcionario_id", "tipo_ausencia_id", "ano"}))
public class SaldoAusenciaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "tipo_ausencia_id", nullable = false)
    private UUID tipoAusenciaId;

    @Column(name = "ano", nullable = false)
    private int ano;

    @Column(name = "dias_direito", nullable = false)
    private int diasDireito;

    @Column(name = "dias_gozados", nullable = false)
    private int diasGozados;

    @Column(name = "dias_pendentes", nullable = false)
    private int diasPendentes;
}
