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

/** Acto a publicar no Boletim Oficial ou na página electrónica (Lei n.º 20/X/2023, arts. 89.º–90.º). Tabela nova, ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsPublicacaoOficialEntity")
@NoArgsConstructor
@Table(name = "t_publicacao_oficial", indexes = {
        @Index(name = "ix_publicacao_oficial_estado", columnList = "estado, data_acto"),
        @Index(name = "ix_publicacao_oficial_referencia", columnList = "referencia_tipo, referencia_id")
})
public class PublicacaoOficialEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "tipo_acto", nullable = false, length = 30)
    private String tipoActo;

    /** BOLETIM_OFICIAL ou PAGINA_ELECTRONICA. */
    @Column(name = "meio", nullable = false, length = 20)
    private String meio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private FuncionarioEntity funcionario;

    @Column(name = "referencia_tipo", length = 40)
    private String referenciaTipo;

    @Column(name = "referencia_id", length = 60)
    private String referenciaId;

    @Column(name = "sumario", nullable = false, length = 2000)
    private String sumario;

    @Column(name = "data_acto", nullable = false)
    private LocalDate dataActo;

    /** A_PUBLICAR, PUBLICADA, CANCELADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extracto_id")
    private DocumentoEmitidoEntity extracto;

    @Column(name = "serie", length = 20)
    private String serie;

    @Column(name = "numero", length = 50)
    private String numero;

    @Column(name = "data_publicacao")
    private LocalDate dataPublicacao;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;
}
