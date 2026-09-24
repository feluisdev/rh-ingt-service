package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * As ferias de um colaborador num ano: preferencia, marcacao e alteracoes (DL n.o 3/2010, arts.
 * 5.o e 6.o). Tabela nova, criada pelo ddl-auto -- sem migracao, por decisao do utilizador; a
 * unicidade (funcionario, ano) vai na anotacao para o Hibernate a criar.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFeriasDoAnoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_ferias_ano",
        uniqueConstraints = @UniqueConstraint(name = "uk_ferias_ano_funcionario_ano",
                columnNames = {"funcionario_id", "ano"}))
public class FeriasDoAnoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "ano", nullable = false)
    private int ano;

    @Column(name = "preferencia_indicada_em")
    private LocalDate preferenciaIndicadaEm;

    @Column(name = "preferencia_fora_de_prazo", nullable = false)
    private boolean preferenciaForaDePrazo;

    @Column(name = "preferencia_observacoes", length = 500)
    private String preferenciaObservacoes;

    /** PROPRIO ou RH. Coluna nova numa tabela do ddl-auto que ainda nao chegou a producao: sem migracao. */
    @Column(name = "preferencia_indicada_por", length = 10)
    private String preferenciaIndicadaPor;

    /** ACORDO (art. 5.o n.o 3) ou FIXADA pelo dirigente (n.o 5). Nulo enquanto nao ha marcacao. */
    @Column(name = "origem", length = 20)
    private String origem;

    @Column(name = "fundamentacao", length = 1000)
    private String fundamentacao;

    @Column(name = "marcada_em")
    private LocalDate marcadaEm;

    @OneToMany(mappedBy = "feriasAno", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataInicio")
    private List<FeriasPeriodoEntity> periodos = new ArrayList<>();

    @OneToMany(mappedBy = "feriasAno", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("alteradaEm")
    private List<FeriasAlteracaoEntity> alteracoes = new ArrayList<>();
}
