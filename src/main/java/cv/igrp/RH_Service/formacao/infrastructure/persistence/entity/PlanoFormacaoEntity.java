package cv.igrp.RH_Service.formacao.infrastructure.persistence.entity;

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

/** O plano anual de formação. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "FormPlanoFormacaoEntity")
@NoArgsConstructor
@Table(name = "t_plano_formacao", indexes = @Index(name = "ix_plano_formacao_ano", columnList = "ano"))
public class PlanoFormacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    /** A unidade orgânica, se o plano é de uma unidade (sem FK física). */
    @Column(name = "unidade_id")
    private UUID unidadeId;

    @Column(name = "designacao", nullable = false, length = 200)
    private String designacao;

    /** RASCUNHO, APROVADO. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "despacho", length = 200)
    private String despacho;

    @Column(name = "data_aprovacao")
    private LocalDate dataAprovacao;

    @OneToMany(mappedBy = "plano", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<NecessidadeFormacaoEntity> necessidades = new ArrayList<>();
}
