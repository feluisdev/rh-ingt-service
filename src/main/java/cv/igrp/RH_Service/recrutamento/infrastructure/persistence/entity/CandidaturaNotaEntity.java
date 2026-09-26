package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.util.UUID;

/** A nota de uma candidatura num método de selecção (0–20). Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "RecrutCandidaturaNotaEntity")
@NoArgsConstructor
@Table(name = "t_candidatura_nota")
public class CandidaturaNotaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidatura_id", nullable = false)
    private CandidaturaEntity candidatura;

    @Column(name = "metodo", nullable = false, length = 30)
    private String metodo;

    @Column(name = "nota", nullable = false, precision = 5, scale = 2)
    private BigDecimal nota;
}
