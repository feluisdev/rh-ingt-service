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

/** Um item de uma checklist (copiado do modelo ou acrescentado). Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsChecklistItemEntity")
@NoArgsConstructor
@Table(name = "t_checklist_item", indexes = {
        @Index(name = "ix_checklist_item_prazo", columnList = "estado, prazo"),
        @Index(name = "ix_checklist_item_responsavel", columnList = "responsavel, estado")
})
public class ChecklistItemEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private ChecklistEntity checklist;

    /** O item do modelo de onde veio (sem FK física: o modelo pode mudar). */
    @Column(name = "modelo_item_id")
    private UUID modeloItemId;

    @Column(name = "codigo", length = 40)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Column(name = "responsavel", nullable = false, length = 20)
    private String responsavel;

    @Column(name = "obrigatorio", nullable = false)
    private Boolean obrigatorio;

    @Column(name = "prazo")
    private LocalDate prazo;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    /** PENDENTE, FEITO, NAO_APLICAVEL. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "observacao", length = 500)
    private String observacao;

    @Column(name = "automatico", nullable = false)
    private Boolean automatico;
}
