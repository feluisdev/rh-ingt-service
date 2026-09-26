package cv.igrp.RH_Service.formacao.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.UUID;

/** Uma necessidade de formação do plano. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "FormNecessidadeFormacaoEntity")
@NoArgsConstructor
@Table(name = "t_necessidade_formacao")
public class NecessidadeFormacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoFormacaoEntity plano;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "tema", nullable = false, length = 300)
    private String tema;

    /** O colaborador a quem a formação faz falta, se é de alguém (sem FK física). */
    @Column(name = "funcionario_id")
    private UUID funcionarioId;

    /** PROPRIO, CHEFIA, RH. */
    @Column(name = "origem", nullable = false, length = 10)
    private String origem;

    /** ALTA, MEDIA, BAIXA. */
    @Column(name = "prioridade", nullable = false, length = 10)
    private String prioridade;

    @Column(name = "justificacao", length = 500)
    private String justificacao;

    /** IDENTIFICADA, PLANEADA, SATISFEITA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "accao_id")
    private UUID accaoId;
}
