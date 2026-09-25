package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Documento emitido pelo RH (declaração, cartão profissional, extracto para publicação): só os metadados —
 * o PDF está no MinIO ({@code ficheiro}), com a impressão digital e o código de verificação aqui. Tabela
 * nova, criada pelo ddl-auto — sem migração.
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsDocumentoEmitidoEntity")
@NoArgsConstructor
@Table(name = "t_documento_emitido", indexes = {
        @Index(name = "ux_documento_emitido_numero", columnList = "numero", unique = true),
        @Index(name = "ux_documento_emitido_codigo", columnList = "codigo_verificacao", unique = true),
        @Index(name = "ix_documento_emitido_funcionario", columnList = "funcionario_id, emitido_em")
})
public class DocumentoEmitidoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    /** DECLARACAO, CARTAO_PROFISSIONAL, EXTRACTO_PUBLICACAO. */
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private FuncionarioEntity funcionario;

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "emitido_em", nullable = false)
    private LocalDateTime emitidoEm;

    @Column(name = "codigo_verificacao", nullable = false, length = 20)
    private String codigoVerificacao;

    /** O caminho do PDF no MinIO. */
    @Column(name = "ficheiro", nullable = false, length = 300)
    private String ficheiro;

    /** SHA-256 do PDF, em hexadecimal. */
    @Column(name = "impressao_digital", nullable = false, length = 64)
    private String impressaoDigital;

    @Column(name = "referencia_tipo", length = 40)
    private String referenciaTipo;

    @Column(name = "referencia_id", length = 60)
    private String referenciaId;

    @Column(name = "anulado_em")
    private LocalDateTime anuladoEm;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;
}
