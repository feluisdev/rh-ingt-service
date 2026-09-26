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

/** Uma exoneração voluntária (Lei n.º 20/X/2023, arts. 94.º e 95.º). Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsExoneracaoEntity")
@NoArgsConstructor
@Table(name = "t_exoneracao", indexes = @Index(name = "ix_exoneracao_estado", columnList = "estado, data_pretendida"))
public class ExoneracaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "data_pre_aviso", nullable = false)
    private LocalDate dataPreAviso;

    @Column(name = "data_pretendida", nullable = false)
    private LocalDate dataPretendida;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "pedida_pelo_proprio", nullable = false)
    private Boolean pedidaPeloProprio;

    /** PEDIDA, DEFERIDA, EFECTIVADA, DESISTIDA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "despacho", length = 200)
    private String despacho;

    @Column(name = "data_despacho")
    private LocalDate dataDespacho;

    @Column(name = "condicionada_ate")
    private LocalDate condicionadaAte;

    @Column(name = "data_efeito")
    private LocalDate dataEfeito;
}
