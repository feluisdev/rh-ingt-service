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

/** Pedido de declaração. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsPedidoDeclaracaoEntity")
@NoArgsConstructor
@Table(name = "t_pedido_declaracao", indexes = {
        @Index(name = "ix_pedido_declaracao_funcionario", columnList = "funcionario_id, data_pedido"),
        @Index(name = "ix_pedido_declaracao_estado", columnList = "estado, data_pedido")
})
public class PedidoDeclaracaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** VINCULO, TEMPO_SERVICO, ANTIGUIDADE_CATEGORIA, SITUACAO_FUNCIONAL. */
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "finalidade", length = 300)
    private String finalidade;

    /** PEDIDA, EMITIDA, RECUSADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "pedido_pelo_proprio", nullable = false)
    private Boolean pedidoPeloProprio;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private DocumentoEmitidoEntity documento;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;
}
