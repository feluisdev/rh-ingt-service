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

/** Estágio probatório ou período experimental (Lei n.º 20/X/2023, arts. 57.º, 72.º, 79.º–81.º). Tabela nova, ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsPeriodoProvaEntity")
@NoArgsConstructor
@Table(name = "t_periodo_prova", indexes = {
        @Index(name = "ix_periodo_prova_funcionario", columnList = "funcionario_id, inicio"),
        @Index(name = "ix_periodo_prova_fim", columnList = "estado, fim_previsto")
})
public class PeriodoProvaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provimento_id", nullable = false)
    private ProvimentoEntity provimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** ESTAGIO_PROBATORIO ou PERIODO_EXPERIMENTAL. */
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim_previsto", nullable = false)
    private LocalDate fimPrevisto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private FuncionarioEntity tutor;

    /** EM_CURSO, CONCLUIDO_COM_SUCESSO, CONCLUIDO_SEM_SUCESSO, CESSADO_ANTECIPADAMENTE, DENUNCIADO. */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "data_relatorio")
    private LocalDate dataRelatorio;

    /** POSITIVA ou NEGATIVA. */
    @Column(name = "avaliacao", length = 20)
    private String avaliacao;

    @Column(name = "fundamentacao", length = 2000)
    private String fundamentacao;

    @Column(name = "data_fim")
    private LocalDate dataFim;
}
