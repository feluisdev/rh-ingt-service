package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Um período de incapacidade temporária de um acidente em serviço. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsAcidenteIncapacidadeEntity")
@NoArgsConstructor
@Table(name = "t_acidente_incapacidade")
public class AcidenteIncapacidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acidente_id", nullable = false)
    private AcidenteServicoEntity acidente;

    /** TEMPORARIA_ABSOLUTA, TEMPORARIA_PARCIAL. */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim")
    private LocalDate fim;
}
