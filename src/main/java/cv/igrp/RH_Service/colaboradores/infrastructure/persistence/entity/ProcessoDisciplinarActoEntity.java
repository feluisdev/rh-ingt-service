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

/** Um acto datado do processo disciplinar. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProcessoDisciplinarActoEntity")
@NoArgsConstructor
@Table(name = "t_processo_disciplinar_acto", indexes = {
        @Index(name = "ix_processo_disciplinar_acto_tipo", columnList = "tipo, data, data_fim")
})
public class ProcessoDisciplinarActoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processo_id", nullable = false)
    private ProcessoDisciplinarEntity processo;

    /** ActoDisciplinar.Tipo. */
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    /** O fim do que o acto abre: a suspensão preventiva, o prazo de defesa, o prazo de recurso. */
    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "dias")
    private Integer dias;

    @Column(name = "pena", length = 30)
    private String pena;

    @Column(name = "duracao")
    private Integer duracao;

    @Column(name = "texto", columnDefinition = "TEXT")
    private String texto;
}
