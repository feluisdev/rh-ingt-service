package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.UUID;

/** Um item do modelo de checklist de entrada/saída. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsItemChecklistModeloEntity")
@NoArgsConstructor
@Table(name = "t_checklist_modelo_item", indexes = {
        @Index(name = "ux_checklist_modelo_codigo", columnList = "tipo, codigo", unique = true)
})
public class ItemChecklistModeloEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** ENTRADA, SAIDA. */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "codigo", length = 40)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    /** RH, CHEFIA, PROPRIO, INFORMATICA, PATRIMONIO. */
    @Column(name = "responsavel", nullable = false, length = 20)
    private String responsavel;

    @Column(name = "obrigatorio", nullable = false)
    private Boolean obrigatorio;

    @Column(name = "prazo_dias")
    private Integer prazoDias;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
