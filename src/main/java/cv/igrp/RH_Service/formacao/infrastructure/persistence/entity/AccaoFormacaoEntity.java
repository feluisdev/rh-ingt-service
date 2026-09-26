package cv.igrp.RH_Service.formacao.infrastructure.persistence.entity;

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

/** Uma acção de formação (Lei n.º 20/X/2023, art. 141.º). Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "FormAccaoFormacaoEntity")
@NoArgsConstructor
@Table(name = "t_accao_formacao", indexes = @Index(name = "ix_accao_formacao_estado", columnList = "estado, inicio"))
public class AccaoFormacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** O plano a que responde (sem FK física). */
    @Column(name = "plano_id")
    private UUID planoId;

    @ElementCollection
    @CollectionTable(name = "t_accao_formacao_necessidade", joinColumns = @JoinColumn(name = "accao_id"))
    @Column(name = "necessidade_id", nullable = false)
    private List<UUID> necessidades = new ArrayList<>();

    @Column(name = "tema", nullable = false, length = 300)
    private String tema;

    @Column(name = "entidade_formadora", length = 200)
    private String entidadeFormadora;

    /** PRESENCIAL, DISTANCIA, MISTA. */
    @Column(name = "modalidade", nullable = false, length = 15)
    private String modalidade;

    @Column(name = "interna", nullable = false)
    private Boolean interna;

    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim", nullable = false)
    private LocalDate fim;

    @Column(name = "horas", nullable = false)
    private Integer horas;

    @Column(name = "horario", length = 200)
    private String horario;

    @Column(name = "local", length = 200)
    private String local;

    @Column(name = "vagas")
    private Integer vagas;

    @Column(name = "custo_previsto", precision = 14, scale = 2)
    private BigDecimal custoPrevisto;

    @Column(name = "custeada_pela_administracao", nullable = false)
    private Boolean custeadaPelaAdministracao;

    @Column(name = "meses_garantia")
    private Integer mesesGarantia;

    /** PLANEADA, INSCRICOES_ABERTAS, EM_CURSO, CONCLUIDA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "accao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data")
    private List<InscricaoFormacaoEntity> inscricoes = new ArrayList<>();
}
