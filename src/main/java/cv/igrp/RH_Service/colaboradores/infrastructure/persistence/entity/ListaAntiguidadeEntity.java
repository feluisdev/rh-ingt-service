package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

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
 * Lista de antiguidade oficial de um serviço num ano (DL n.º 3/2010, arts. 69.º–74.º), com as linhas congeladas
 * na aprovação. A unidade é referida por UUID sem FK (a lista é um documento: sobrevive à reorganização).
 * Tabela nova, criada pelo ddl-auto — sem migração.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsListaAntiguidadeEntity")
@NoArgsConstructor
@Table(name = "t_lista_antiguidade",
        indexes = @Index(name = "ix_lista_antiguidade_ano_unidade", columnList = "ano, unidade_id"))
public class ListaAntiguidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "referencia", nullable = false)
    private LocalDate referencia;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "incluir_subunidades", nullable = false)
    private Boolean incluirSubunidades;

    /** APROVADA, AFIXADA, DEFINITIVA, PUBLICADA, ANULADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "aprovada_por", nullable = false, length = 200)
    private String aprovadaPor;

    @Column(name = "data_aprovacao", nullable = false)
    private LocalDate dataAprovacao;

    @Column(name = "data_afixacao")
    private LocalDate dataAfixacao;

    @Column(name = "local_afixacao", length = 300)
    private String localAfixacao;

    @Column(name = "data_definitiva")
    private LocalDate dataDefinitiva;

    @Column(name = "publicacao_serie", length = 20)
    private String publicacaoSerie;

    @Column(name = "publicacao_numero", length = 50)
    private String publicacaoNumero;

    @Column(name = "publicacao_data")
    private LocalDate publicacaoData;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("grupo, posicao")
    private List<ListaAntiguidadeLinhaEntity> linhas = new ArrayList<>();
}
