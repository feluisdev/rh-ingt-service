package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity;

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

/**
 * Procedimento concursal (Lei n.º 20/X/2023, arts. 123.º–129.º). A categoria e os Lugares referem-se por UUID sem FK
 * física (o módulo de recrutamento não se amarra às tabelas da estrutura). Tabela nova, criada pelo ddl-auto.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "RecrutConcursoEntity")
@NoArgsConstructor
@Table(name = "t_concurso", indexes = @Index(name = "ux_concurso_referencia", columnList = "referencia", unique = true))
public class ConcursoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "referencia", nullable = false, length = 60)
    private String referencia;

    /** INGRESSO ou ACESSO. */
    @Column(name = "finalidade", nullable = false, length = 20)
    private String finalidade;

    /** COMUM ou ESPECIAL. */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    /** EXTERNO, INTERNO, INTERNO_RESTRITO. */
    @Column(name = "modalidade", nullable = false, length = 20)
    private String modalidade;

    @Column(name = "vinculo", length = 30)
    private String vinculo;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @ElementCollection
    @CollectionTable(name = "t_concurso_lugar", joinColumns = @JoinColumn(name = "concurso_id"))
    @Column(name = "position_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<UUID> lugares = new ArrayList<>();

    @Column(name = "requisitos", length = 4000)
    private String requisitos;

    @Column(name = "habilitacao_minima", length = 500)
    private String habilitacaoMinima;

    @Column(name = "quota_deficiencia")
    private Integer quotaDeficiencia;

    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<ConcursoMetodoEntity> metodos = new ArrayList<>();

    @Column(name = "dispensa_metodos_despacho", length = 200)
    private String dispensaMetodosDespacho;

    @OneToMany(mappedBy = "concurso", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<ConcursoJuriEntity> juri = new ArrayList<>();

    @Column(name = "data_aviso")
    private LocalDate dataAviso;

    @Column(name = "candidaturas_de")
    private LocalDate candidaturasDe;

    @Column(name = "candidaturas_ate")
    private LocalDate candidaturasAte;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "homologacao_despacho", length = 100)
    private String homologacaoDespacho;

    @Column(name = "homologacao_data")
    private LocalDate homologacaoData;

    @Column(name = "reserva_ate")
    private LocalDate reservaAte;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;
}
