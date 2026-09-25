package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Diário de factos do RH para o processamento salarial (BR-FAC-01..05). Só cresce: cada linha é escrita
 * uma vez e nunca muda — por isso não é auditada pelo Envers (é ela própria um registo).
 * Tabela nova, criada pelo ddl-auto — sem migração.
 */
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFactoRhEntity")
@NoArgsConstructor
@Table(name = "t_facto_rh", indexes = {
        @Index(name = "ix_facto_rh_mes", columnList = "mes_competencia, registado_em"),
        @Index(name = "ix_facto_rh_funcionario", columnList = "funcionario_id, data_efeito")
})
public class FactoRhEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** O nome de {@code TipoFactoRh}. */
    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "data_efeito", nullable = false)
    private LocalDate dataEfeito;

    /** yyyy-MM: o mês de processamento em que entra. */
    @Column(name = "mes_competencia", nullable = false, length = 7)
    private String mesCompetencia;

    @Column(name = "referencia_tipo", length = 40)
    private String referenciaTipo;

    @Column(name = "referencia_id", length = 60)
    private String referenciaId;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados", columnDefinition = "jsonb")
    private Map<String, String> dados;

    @Column(name = "registado_em", nullable = false)
    private LocalDateTime registadoEm;
}
