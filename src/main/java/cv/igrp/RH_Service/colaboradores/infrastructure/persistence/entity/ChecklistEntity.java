package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A checklist de entrada ou de saída de um colaborador. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsChecklistEntity")
@NoArgsConstructor
@Table(name = "t_checklist", indexes = {
        @Index(name = "ix_checklist_funcionario", columnList = "funcionario_id, tipo, estado"),
        @Index(name = "ix_checklist_estado", columnList = "estado")
})
public class ChecklistEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** ENTRADA, SAIDA. */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    /** ABERTA, CONCLUIDA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "aberta_em", nullable = false)
    private LocalDate abertaEm;

    @Column(name = "concluida_em")
    private LocalDate concluidaEm;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<ChecklistItemEntity> itens = new ArrayList<>();
}
