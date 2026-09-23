package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Um periodo de ferias, da preferencia ou da marcacao (natureza). */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFeriasPeriodoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_ferias_ano_periodo")
public class FeriasPeriodoEntity extends AuditEntity {

    public static final String PREFERENCIA = "PREFERENCIA";
    public static final String MARCACAO = "MARCACAO";

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ferias_ano_id", nullable = false)
    private FeriasDoAnoEntity feriasAno;

    /** PREFERENCIA ou MARCACAO. */
    @Column(name = "natureza", nullable = false, length = 20)
    private String natureza;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    /** So na marcacao: dias uteis, contados com o calendario de feriados do colaborador. */
    @Column(name = "dias_uteis")
    private Integer diasUteis;
}
