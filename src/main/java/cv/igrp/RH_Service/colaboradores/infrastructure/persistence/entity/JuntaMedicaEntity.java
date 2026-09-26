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

/** Um pedido e parecer de junta médica (comissão de verificação de incapacidade). Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsJuntaMedicaEntity")
@NoArgsConstructor
@Table(name = "t_junta_medica", indexes = @Index(name = "ix_junta_medica_estado", columnList = "estado, data_pedido"))
public class JuntaMedicaEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "motivo", nullable = false, length = 25)
    private String motivo;

    @Column(name = "fundamentacao", length = 1000)
    private String fundamentacao;

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_junta")
    private LocalDate dataJunta;

    /** APTO, APTO_OUTRAS_FUNCOES, INCAPAZ_TEMPORARIO, INCAPAZ_PERMANENTE. */
    @Column(name = "parecer", length = 25)
    private String parecer;

    @Column(name = "dias_incapacidade")
    private Integer diasIncapacidade;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    /** PEDIDA, REALIZADA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;
}
