package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabela nova: nasce pelo ddl-auto, sem migração. As invariantes estão no domínio (Horario). */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_horario")
public class HorarioEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    /** FIXO ou FLEXIVEL. */
    @Column(name = "controlo", nullable = false, length = 20)
    private String controlo;

    /** SEMANA ou MES; só no flexível. */
    @Column(name = "periodo_afericao", length = 10)
    private String periodoAfericao;

    /** Só no flexível. */
    @Column(name = "duracao_diaria_minutos")
    private Integer duracaoDiariaMinutos;

    /** O horário da instituição: no máximo um (aplicacional). */
    @Column(name = "is_base", nullable = false)
    private Boolean isBase;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("diaSemana, horaInicio")
    private List<HorarioBlocoEntity> blocos = new ArrayList<>();
}
