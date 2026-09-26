package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

/** O fecho de um mês de processamento, com a relação mensal congelada. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFechoMensalEntity")
@NoArgsConstructor
@Table(name = "t_fecho_mensal", indexes = @Index(name = "ux_fecho_mensal_mes", columnList = "mes", unique = true))
public class FechoMensalEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** AAAA-MM. */
    @Column(name = "mes", nullable = false, length = 7)
    private String mes;

    /** FECHADO, REABERTO. */
    @Column(name = "estado", nullable = false, length = 10)
    private String estado;

    @Column(name = "fechado_em", nullable = false)
    private LocalDateTime fechadoEm;

    @Column(name = "fechos", nullable = false)
    private Integer fechos;

    @Column(name = "motivo_reabertura", length = 500)
    private String motivoReabertura;

    @Column(name = "reaberto_em")
    private LocalDateTime reabertoEm;

    @Column(name = "relacao", columnDefinition = "TEXT")
    private String relacao;

    @Column(name = "total_factos", nullable = false)
    private Integer totalFactos;
}
