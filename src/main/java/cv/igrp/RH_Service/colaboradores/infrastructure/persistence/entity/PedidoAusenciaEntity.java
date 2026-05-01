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
@Entity(name = "ColabsPedidoAusenciaEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_request")
public class PedidoAusenciaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "tipo_ausencia_id", nullable = false)
    private UUID tipoAusenciaId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "numero_dias", nullable = false)
    private int numeroDias;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "aprovado_por")
    private UUID aprovadoPor;

    @Column(name = "data_decisao")
    private LocalDate dataDecisao;

    @Column(name = "observacoes_decisao")
    private String observacoesDecisao;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
