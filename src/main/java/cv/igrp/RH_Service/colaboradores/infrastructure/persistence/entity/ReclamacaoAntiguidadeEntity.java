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

/** Reclamação da lista de antiguidade (DL n.º 3/2010, arts. 72.º–74.º). Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsReclamacaoAntiguidadeEntity")
@NoArgsConstructor
@Table(name = "t_reclamacao_antiguidade",
        indexes = @Index(name = "ix_reclamacao_antiguidade_lista", columnList = "lista_id, data_apresentacao"))
public class ReclamacaoAntiguidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaAntiguidadeEntity lista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** OMISSAO, GRADUACAO, SITUACAO, CONTAGEM. */
    @Column(name = "fundamento", nullable = false, length = 20)
    private String fundamento;

    @Column(name = "texto", nullable = false, length = 2000)
    private String texto;

    @Column(name = "no_estrangeiro", nullable = false)
    private Boolean noEstrangeiro;

    @Column(name = "pelo_proprio", nullable = false)
    private Boolean peloProprio;

    @Column(name = "data_apresentacao", nullable = false)
    private LocalDate dataApresentacao;

    /** APRESENTADA, DEFERIDA, INDEFERIDA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "decisao", length = 2000)
    private String decisao;

    @Column(name = "data_decisao")
    private LocalDate dataDecisao;

    @Column(name = "recurso_em")
    private LocalDate recursoEm;

    @Column(name = "recurso_texto", length = 2000)
    private String recursoTexto;

    /** PROVIDO, NAO_PROVIDO. */
    @Column(name = "recurso_resultado", length = 20)
    private String recursoResultado;

    @Column(name = "recurso_decisao", length = 2000)
    private String recursoDecisao;

    @Column(name = "recurso_decidido_em")
    private LocalDate recursoDecididoEm;
}
