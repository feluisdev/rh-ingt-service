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
 * Permanência ao serviço para além dos 65 anos (Lei n.º 20/X/2023, art. 48.º n.os 2 e 3). Tabela nova,
 * criada pelo ddl-auto — sem migração.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProrrogacaoPermanenciaEntity")
@NoArgsConstructor
@Table(name = "t_prorrogacao_permanencia",
        indexes = @Index(name = "ix_prorrogacao_permanencia_funcionario", columnList = "funcionario_id, data_pedido"))
public class ProrrogacaoPermanenciaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** PEDIDA, AUTORIZADA, INDEFERIDA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "proposta_fundamentada", nullable = false, length = 1000)
    private String propostaFundamentada;

    @Column(name = "valida_ate", nullable = false)
    private LocalDate validaAte;

    @Column(name = "despacho_numero", length = 100)
    private String despachoNumero;

    @Column(name = "despacho_data")
    private LocalDate despachoData;

    @Column(name = "motivo_indeferimento", length = 500)
    private String motivoIndeferimento;
}
