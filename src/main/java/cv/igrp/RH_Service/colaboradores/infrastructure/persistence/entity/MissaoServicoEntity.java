package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Uma missão de serviço (Lei n.º 20/X/2023, art. 159.º). Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsMissaoServicoEntity")
@NoArgsConstructor
@Table(name = "t_missao_servico", indexes = @Index(name = "ix_missao_servico_estado", columnList = "estado, partida"))
public class MissaoServicoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** Os participantes (funcionários; sem FK física na colecção). */
    @ElementCollection
    @CollectionTable(name = "t_missao_servico_participante", joinColumns = @JoinColumn(name = "missao_id"),
            indexes = @Index(name = "ix_missao_participante", columnList = "funcionario_id"))
    @Column(name = "funcionario_id", nullable = false)
    private List<UUID> participantes = new ArrayList<>();

    /** NACIONAL, ESTRANGEIRO. */
    @Column(name = "destino_tipo", nullable = false, length = 15)
    private String destinoTipo;

    @Column(name = "destino", nullable = false, length = 200)
    private String destino;

    @Column(name = "objectivo", nullable = false, length = 1000)
    private String objectivo;

    @Column(name = "partida", nullable = false)
    private LocalDateTime partida;

    @Column(name = "regresso", nullable = false)
    private LocalDateTime regresso;

    @Column(name = "transporte", nullable = false, length = 20)
    private String transporte;

    @Column(name = "alojamento_a_cargo", nullable = false)
    private Boolean alojamentoACargo;

    @Column(name = "adiantamento", nullable = false)
    private Boolean adiantamento;

    /** PEDIDA, AUTORIZADA, RECUSADA, REALIZADA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "pedido_por")
    private UUID pedidoPor;

    @Column(name = "despacho", length = 200)
    private String despacho;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "relatorio", columnDefinition = "TEXT")
    private String relatorio;

    @Column(name = "data_relatorio")
    private LocalDate dataRelatorio;
}
