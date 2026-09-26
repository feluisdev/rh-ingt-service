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

/** Um exame de medicina do trabalho — só a aptidão, sem dados clínicos. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsExameSaudeEntity")
@NoArgsConstructor
@Table(name = "t_exame_saude", indexes = {
        @Index(name = "ix_exame_saude_funcionario", columnList = "funcionario_id, data"),
        @Index(name = "ix_exame_saude_validade", columnList = "validade_ate")
})
public class ExameSaudeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** ADMISSAO, PERIODICO, OCASIONAL, REGRESSO. */
    @Column(name = "tipo", nullable = false, length = 15)
    private String tipo;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "entidade", length = 200)
    private String entidade;

    /** APTO, APTO_CONDICIONADO, INAPTO_TEMPORARIO, INAPTO_DEFINITIVO. */
    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "restricoes", length = 500)
    private String restricoes;

    @Column(name = "validade_ate")
    private LocalDate validadeAte;

    @Column(name = "observacoes", length = 500)
    private String observacoes;
}
