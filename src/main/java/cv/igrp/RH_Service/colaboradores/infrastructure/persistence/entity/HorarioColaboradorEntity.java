package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * O horário atribuído a um colaborador, por período. Tabela nova, criada pelo ddl-auto — sem
 * migração; a não-sobreposição está no domínio (HorarioColaborador).
 */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsHorarioColaboradorEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_horario_colaborador")
public class HorarioColaboradorEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    /** Horário do catálogo (parametrizacoes). */
    @Column(name = "horario_id", nullable = false)
    private UUID horarioId;

    /** PRESENCIAL, TELETRABALHO ou MISTO (art. 166.o). */
    @Column(name = "regime_prestacao", nullable = false, length = 20)
    private String regimePrestacao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;
}
