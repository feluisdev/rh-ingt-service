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

/** Cartão de identificação profissional (Lei n.º 20/X/2023, art. 25.º). Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsCartaoProfissionalEntity")
@NoArgsConstructor
@Table(name = "t_cartao_profissional",
        indexes = @Index(name = "ix_cartao_profissional_funcionario", columnList = "funcionario_id, emitido_em"))
public class CartaoProfissionalEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoEmitidoEntity documento;

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @Column(name = "emitido_em", nullable = false)
    private LocalDate emitidoEm;

    /** A categoria e a função com que foi emitido (a validade depende delas, art. 25.º n.º 5). */
    @Column(name = "categoria_id", length = 60)
    private String categoriaId;

    @Column(name = "categoria", length = 200)
    private String categoria;

    @Column(name = "funcao_id", length = 60)
    private String funcaoId;

    @Column(name = "funcao", length = 200)
    private String funcao;

    @Column(name = "cargo", length = 200)
    private String cargo;

    /** EMITIDO, ENTREGUE, DEVOLVIDO, ANULADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "data_devolucao")
    private LocalDate dataDevolucao;

    @Column(name = "motivo_anulacao", length = 500)
    private String motivoAnulacao;
}
