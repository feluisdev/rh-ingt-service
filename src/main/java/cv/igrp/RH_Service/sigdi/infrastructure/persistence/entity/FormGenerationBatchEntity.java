package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

// Mantida à mão, não gerada pelo iGRP Studio, sem manifesto correspondente em
// .igrpstudio/sigdi/entity/. Ver 119-01-PLAN.md.
//
// Deliberadamente sem a anotacao de auditoria do Envers e sem herdar da base de auditoria
// (D-02, 119-01-PLAN.md). O lote e ele proprio o registo de auditoria; uma sombra Envers de um
// rasto append-only nao acrescentaria nada e obrigaria a criar audit_schema.t_form_generation_batch_aud
// na mesma migracao. As colunas de autoria (generatedBy/generatedAt) sao escritas explicitamente
// pelo codigo da aplicacao -- se acrescentares essa anotacao por simetria com os vizinhos,
// estas a criar uma sombra sem sentido.

import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_form_generation_batch")
public class FormGenerationBatchEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "generation_mode", nullable = false)
    private String generationMode;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_count", nullable = false)
    private Integer createdCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    @Column(name = "skipped_count", nullable = false)
    private Integer skippedCount = 0;

    @Column(name = "pending_count", nullable = false)
    private Integer pendingCount = 0;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "generated_by", nullable = false)
    private String generatedBy;
}
