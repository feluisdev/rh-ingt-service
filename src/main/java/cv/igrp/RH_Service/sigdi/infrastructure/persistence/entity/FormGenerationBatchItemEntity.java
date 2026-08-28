package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

// Mantida à mão, não gerada pelo iGRP Studio, sem manifesto correspondente em
// .igrpstudio/sigdi/entity/. Ver 119-01-PLAN.md.
//
// Deliberadamente sem a anotacao de auditoria do Envers e sem herdar da base de auditoria
// (D-02, 119-01-PLAN.md) -- mesma razao da entidade irma FormGenerationBatchEntity: o lote
// e ele proprio o registo de auditoria.
//
// 119-08: length= explicito em outcome/skipReason, e columnDefinition TEXT em errorMessage.
// Medido contra base real que a V34 foi aplicada com sucesso e mesmo assim o Hibernate
// (ddl-auto=update em development) reescreveu estas tres colunas para varchar(255) por baixo
// dela -- error_message em varchar(255) e o defeito medido no 119-07 que faz o lote inteiro
// falhar ao gravar quando uma mensagem de excepcao excede esse limite. Sem estas anotacoes, o
// ddl-auto=update desfaria a V35 no arranque seguinte, repetindo o mesmo defeito.

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
@Table(name = "t_form_generation_batch_item")
public class FormGenerationBatchItemEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    // Anulavel de proposito: uma linha de unidade organica saltada nao tem colaborador.
    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "outcome", length = 30, nullable = false)
    private String outcome;

    @Column(name = "generated_form_id")
    private UUID generatedFormId;

    @Column(name = "evaluator_id")
    private UUID evaluatorId;

    @Column(name = "skip_reason", length = 50)
    private String skipReason;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Fase 120, plano 01 (PRZ-04, V36) -- duas colunas de reversao, anulaveis. Escritas so pelo
    // update em bloco de FormGenerationBatchRepositoryImpl.markReverted, nunca pelo insert do
    // item (toItemEntity continua a deixa-las a null). revert_skip_reason leva length=50
    // EXPLICITO, na mesma anotacao: e a licao medida da V35 -- sem length=, o ddl-auto=update
    // do perfil development reescreveria a coluna para varchar(255) no arranque seguinte,
    // desfazendo a V36 sem o Flyway dar por nada.
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    @Column(name = "revert_skip_reason", length = 50)
    private String revertSkipReason;
}
