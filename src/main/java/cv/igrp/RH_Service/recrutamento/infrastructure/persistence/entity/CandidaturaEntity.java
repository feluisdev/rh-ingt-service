package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Candidatura a um concurso, com os dados do candidato e as notas. Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "RecrutCandidaturaEntity")
@NoArgsConstructor
@Table(name = "t_candidatura", indexes = {
        @Index(name = "ix_candidatura_concurso", columnList = "concurso_id, data_apresentacao"),
        @Index(name = "ix_candidatura_funcionario", columnList = "funcionario_id")
})
public class CandidaturaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concurso_id", nullable = false)
    private ConcursoEntity concurso;

    @Column(name = "nome", nullable = false, length = 300)
    private String nome;

    @Column(name = "documento", length = 60)
    private String documento;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "telefone", length = 40)
    private String telefone;

    @Column(name = "habilitacao", length = 300)
    private String habilitacao;

    @Column(name = "deficiencia", nullable = false)
    private Boolean deficiencia;

    /** Candidato que é funcionário da casa (sem FK física). */
    @Column(name = "funcionario_id")
    private UUID funcionarioId;

    @Column(name = "vinculado_administracao", nullable = false)
    private Boolean vinculadoAdministracao;

    @Column(name = "data_apresentacao", nullable = false)
    private LocalDate dataApresentacao;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo_exclusao", length = 1000)
    private String motivoExclusao;

    @Column(name = "audiencia_ate")
    private LocalDate audienciaAte;

    @Column(name = "resposta_audiencia", length = 2000)
    private String respostaAudiencia;

    @OneToMany(mappedBy = "candidatura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidaturaNotaEntity> notas = new ArrayList<>();

    @Column(name = "classificacao_final", precision = 5, scale = 2)
    private BigDecimal classificacaoFinal;

    @Column(name = "posicao")
    private Integer posicao;

    /** O Lugar em que foi provido (sem FK física). */
    @Column(name = "lugar_provido_id")
    private UUID lugarProvidoId;

    @Column(name = "data_desistencia")
    private LocalDate dataDesistencia;
}
