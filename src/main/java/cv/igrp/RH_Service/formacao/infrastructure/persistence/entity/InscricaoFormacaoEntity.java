package cv.igrp.RH_Service.formacao.infrastructure.persistence.entity;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Uma inscrição numa acção de formação. Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "FormInscricaoFormacaoEntity")
@NoArgsConstructor
@Table(name = "t_inscricao_formacao", indexes = {
        @Index(name = "ix_inscricao_formacao_funcionario", columnList = "funcionario_id, estado"),
        @Index(name = "ix_inscricao_formacao_garantia", columnList = "garantia_ate")
})
public class InscricaoFormacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accao_id", nullable = false)
    private AccaoFormacaoEntity accao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** PROPRIO, CHEFIA, RH. */
    @Column(name = "origem", nullable = false, length = 10)
    private String origem;

    /** PEDIDA, ADMITIDA, RECUSADA, DESISTIU, APROVEITAMENTO, SEM_APROVEITAMENTO, FALTOU. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "dias_presenca")
    private Integer diasPresenca;

    /** Art. 95.º b): até quando o formando fica a dever permanência. */
    @Column(name = "garantia_ate")
    private LocalDate garantiaAte;
}
