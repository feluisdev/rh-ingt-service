package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma marcação de assiduidade (art. 164.o n.o 3 da Lei n.o 20/X/2023). Nunca se apaga: anula-se.
 * Tabela nova, criada pelo ddl-auto -- sem migracao.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsMarcacaoAssiduidadeEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_marcacao_assiduidade",
        indexes = @Index(name = "ix_marcacao_funcionario_momento", columnList = "funcionario_id, momento"))
public class MarcacaoAssiduidadeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "momento", nullable = false)
    private LocalDateTime momento;

    /** ENTRADA ou SAIDA. */
    @Column(name = "sentido", nullable = false, length = 10)
    private String sentido;

    /** MANUAL, IMPORTADO ou PROPRIO. */
    @Column(name = "origem", nullable = false, length = 20)
    private String origem;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "referencia_externa", length = 100, unique = true)
    private String referenciaExterna;

    @Column(name = "anulada", nullable = false)
    private Boolean anulada;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;

    @Column(name = "anulada_em")
    private LocalDateTime anuladaEm;

    /**
     * VALIDA, PENDENTE ou REJEITADA. Coluna nova numa tabela do ddl-auto que nunca chegou a produção:
     * sem migração. Nula nas linhas de antes, que se lêem VALIDA; toda a escrita a preenche.
     */
    @Column(name = "estado", length = 12)
    private String estado;

    /** A chefia directa que decidiu a correcção; nulo quando foi o RH. */
    @Column(name = "decidida_por")
    private UUID decididaPor;

    @Column(name = "decidida_em")
    private LocalDateTime decididaEm;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;
}
