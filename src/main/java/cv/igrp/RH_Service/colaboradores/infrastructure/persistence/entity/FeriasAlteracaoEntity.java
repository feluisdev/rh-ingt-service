package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Alteracao a uma marcacao depois de o mapa ter sido dado a conhecer (art. 6.o n.o 2). */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFeriasAlteracaoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_ferias_ano_alteracao")
public class FeriasAlteracaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ferias_ano_id", nullable = false)
    private FeriasDoAnoEntity feriasAno;

    /** ACORDO ou CONVENIENCIA_SERVICO. */
    @Column(name = "motivo", nullable = false, length = 30)
    private String motivo;

    @Column(name = "fundamentacao", length = 1000)
    private String fundamentacao;

    @Column(name = "periodos_anteriores", length = 1000)
    private String periodosAnteriores;

    @Column(name = "periodos_novos", length = 1000)
    private String periodosNovos;

    @Column(name = "alterada_em", nullable = false)
    private LocalDate alteradaEm;
}
