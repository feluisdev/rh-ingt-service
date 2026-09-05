package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveMobilitySubtypeEntity;
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
@Entity(name = "ColabsLicencaMobilidadeEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_mobility")
public class LicencaMobilidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subtipo_id", nullable = false)
    private LeaveMobilitySubtypeEntity subtipo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "entidade_destino", length = 200)
    private String entidadeDestino;

    @Column(name = "despacho_numero", length = 100)
    private String despachoNumero;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_unit_id")
    private OrganizationalUnitEntity destinationUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_position_id")
    private PositionEntity destinationPosition;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private DocumentoEntity document;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
