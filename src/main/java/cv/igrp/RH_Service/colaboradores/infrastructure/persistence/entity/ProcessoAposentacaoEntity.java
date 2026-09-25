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
import java.util.UUID;

/**
 * Processo de aposentação (Lei n.º 20/X/2023, arts. 48.º, 173.º–179.º, 189.º). Tabela nova, criada pelo
 * ddl-auto — sem migração.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProcessoAposentacaoEntity")
@NoArgsConstructor
@Table(name = "t_processo_aposentacao",
        indexes = @Index(name = "ix_processo_aposentacao_funcionario", columnList = "funcionario_id, data_pedido"))
public class ProcessoAposentacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** LIMITE_IDADE, ANTECIPADA_PEDIDO, ANTECIPADA_INTERESSE_ADMINISTRACAO, INVALIDEZ, PRE_APOSENTACAO, COMPULSIVA. */
    @Column(name = "modalidade", nullable = false, length = 40)
    private String modalidade;

    /** PEDIDO, DEFERIDO, INDEFERIDO, DESLIGADO, CONCLUIDO, CANCELADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /** FUNCIONARIO ou ADMINISTRACAO. */
    @Column(name = "iniciativa", nullable = false, length = 20)
    private String iniciativa;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "fundamentacao", length = 1000)
    private String fundamentacao;

    @Column(name = "acordo_funcionario", nullable = false)
    private Boolean acordoFuncionario;

    @Column(name = "despacho_numero", length = 100)
    private String despachoNumero;

    @Column(name = "despacho_data")
    private LocalDate despachoData;

    @Column(name = "motivo_indeferimento", length = 500)
    private String motivoIndeferimento;

    @Column(name = "data_desligacao")
    private LocalDate dataDesligacao;

    @Column(name = "percentagem_prestacao", precision = 5, scale = 2)
    private BigDecimal percentagemPrestacao;

    @Column(name = "data_aposentacao")
    private LocalDate dataAposentacao;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;
}
