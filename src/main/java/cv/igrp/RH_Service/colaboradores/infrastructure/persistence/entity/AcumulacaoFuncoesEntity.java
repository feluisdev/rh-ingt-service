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

/** Uma acumulação de funções (Lei n.º 20/X/2023, arts. 20.º–24.º). Tabela nova, criada pelo ddl-auto — sem migração. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsAcumulacaoFuncoesEntity")
@NoArgsConstructor
@Table(name = "t_acumulacao_funcoes", indexes = {
        @Index(name = "ix_acumulacao_funcionario", columnList = "funcionario_id, estado"),
        @Index(name = "ix_acumulacao_estado_fim", columnList = "estado, fim")
})
public class AcumulacaoFuncoesEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** PUBLICA, PRIVADA. */
    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    /** Art. 21.º n.º 2 (nas públicas). */
    @Column(name = "caso_publico", length = 30)
    private String casoPublico;

    @Column(name = "remunerada", nullable = false)
    private Boolean remunerada;

    @Column(name = "entidade", nullable = false, length = 200)
    private String entidade;

    @Column(name = "funcoes", nullable = false, length = 1000)
    private String funcoes;

    @Column(name = "horario", length = 200)
    private String horario;

    @Column(name = "horas_semanais")
    private Integer horasSemanais;

    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim")
    private LocalDate fim;

    @Column(name = "declaracao_sem_conflito", nullable = false)
    private Boolean declaracaoSemConflito;

    /** PEDIDA, AUTORIZADA, INDEFERIDA, CESSADA, CADUCADA. */
    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "despacho", length = 200)
    private String despacho;

    @Column(name = "data_despacho")
    private LocalDate dataDespacho;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "data_fim_efectiva")
    private LocalDate dataFimEfectiva;
}
