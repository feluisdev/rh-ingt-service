package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Um acidente em serviço ou doença profissional (Lei n.º 20/X/2023, arts. 187.º–191.º). Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsAcidenteServicoEntity")
@NoArgsConstructor
@Table(name = "t_acidente_servico", indexes = @Index(name = "ix_acidente_servico_funcionario", columnList = "funcionario_id, estado"))
public class AcidenteServicoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** ACIDENTE_SERVICO, ACIDENTE_TRAJECTO, DOENCA_PROFISSIONAL. */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "local", length = 300)
    private String local;

    @Column(name = "descricao", nullable = false, length = 2000)
    private String descricao;

    @Column(name = "testemunhas", length = 1000)
    private String testemunhas;

    @Column(name = "data_participacao", nullable = false)
    private LocalDate dataParticipacao;

    @Column(name = "participado_pelo_proprio", nullable = false)
    private Boolean participadoPeloProprio;

    /** PARTICIPADO, QUALIFICADO, NAO_QUALIFICADO, ENCERRADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "despacho", length = 200)
    private String despacho;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "seguradora", length = 200)
    private String seguradora;

    @Column(name = "apolice", length = 100)
    private String apolice;

    @Column(name = "participacao_seguradora")
    private LocalDate participacaoSeguradora;

    @Column(name = "alta")
    private LocalDate alta;

    @Column(name = "incapacidade_permanente", precision = 5, scale = 2)
    private BigDecimal incapacidadePermanente;

    @Column(name = "incapacidade_absoluta", nullable = false)
    private Boolean incapacidadeAbsoluta;

    @OneToMany(mappedBy = "acidente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("inicio")
    private List<AcidenteIncapacidadeEntity> incapacidades = new ArrayList<>();
}
