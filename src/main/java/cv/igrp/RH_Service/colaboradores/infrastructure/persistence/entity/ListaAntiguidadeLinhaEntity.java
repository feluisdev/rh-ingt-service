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

/**
 * Uma linha congelada da lista de antiguidade: o que o art. 69.º n.º 2 do DL n.º 3/2010 manda mostrar, tal
 * como estava na aprovação (o nome e o número também — é um documento). Tabela nova, criada pelo ddl-auto.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsListaAntiguidadeLinhaEntity")
@NoArgsConstructor
@Table(name = "t_lista_antiguidade_linha")
public class ListaAntiguidadeLinhaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_id", nullable = false)
    private ListaAntiguidadeEntity lista;

    @Column(name = "grupo", nullable = false)
    private Integer grupo;

    @Column(name = "carreira", length = 200)
    private String carreira;

    @Column(name = "categoria", length = 200)
    private String categoria;

    @Column(name = "fora_de_grelha", nullable = false)
    private Boolean foraDeGrelha;

    @Column(name = "posicao", nullable = false)
    private Integer posicao;

    /** O colaborador (sem FK: a linha é um documento). */
    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "numero_funcionario", length = 30)
    private String numeroFuncionario;

    @Column(name = "nome", length = 300)
    private String nome;

    @Column(name = "unidade_id")
    private UUID unidadeId;

    @Column(name = "escalao", length = 100)
    private String escalao;

    @Column(name = "inicio_no_cargo")
    private LocalDate inicioNoCargo;

    @Column(name = "dias_descontados", nullable = false)
    private Long diasDescontados;

    @Column(name = "dias_no_cargo", nullable = false)
    private Long diasNoCargo;

    @Column(name = "anos_no_cargo", nullable = false)
    private Integer anosNoCargo;

    @Column(name = "meses_no_cargo", nullable = false)
    private Integer mesesNoCargo;

    @Column(name = "dias_no_cargo_resto", nullable = false)
    private Integer diasNoCargoResto;

    @Column(name = "dias_totais", nullable = false)
    private Long diasTotais;

    @Column(name = "anos_total", nullable = false)
    private Integer anosTotal;

    @Column(name = "meses_total", nullable = false)
    private Integer mesesTotal;

    @Column(name = "dias_total_resto", nullable = false)
    private Integer diasTotalResto;
}
