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

/** Um método de selecção do concurso, com a ponderação (Lei n.º 20/X/2023, art. 128.º). Tabela nova, ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "RecrutConcursoMetodoEntity")
@NoArgsConstructor
@Table(name = "t_concurso_metodo")
public class ConcursoMetodoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concurso_id", nullable = false)
    private ConcursoEntity concurso;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "metodo", nullable = false, length = 30)
    private String metodo;

    @Column(name = "ponderacao", nullable = false)
    private Integer ponderacao;

    @Column(name = "eliminatorio", nullable = false)
    private Boolean eliminatorio;

    @Column(name = "nota_minima", precision = 5, scale = 2)
    private BigDecimal notaMinima;
}
