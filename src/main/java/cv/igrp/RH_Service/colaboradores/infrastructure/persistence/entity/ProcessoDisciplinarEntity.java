package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProcessoDisciplinarEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_disciplinary_process")
public class ProcessoDisciplinarEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "process_number", length = 100)
    private String processNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "penalty", length = 255)
    private String penalty;

    @Column(name = "penalty_start_date")
    private LocalDate penaltyStartDate;

    @Column(name = "penalty_end_date")
    private LocalDate penaltyEndDate;

    @Column(name = "official_bulletin", length = 255)
    private String officialBulletin;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;


    // Tramitação (V61; nulas nos registos antigos) -------------------------------------------------

    /** EspecieProcessoDisciplinar. */
    @Column(name = "especie", length = 30)
    private String especie;

    /** FaseProcessoDisciplinar; nula = registo antigo sem tramitação. */
    @Column(name = "fase", length = 20)
    private String fase;

    @Column(name = "data_infraccao")
    private LocalDate dataInfraccao;

    /** A pena prevista ou a da acusação (PenaDisciplinar). */
    @Column(name = "pena_prevista", length = 30)
    private String penaPrevista;

    @Column(name = "instrutor_id")
    private UUID instrutorId;

    @Column(name = "instrutor_nome", length = 200)
    private String instrutorNome;

    /** A pena aplicada (PenaDisciplinar). */
    @Column(name = "pena", length = 30)
    private String pena;

    /** Dias de multa ou de suspensão; meses de inactividade. */
    @Column(name = "pena_duracao")
    private Integer penaDuracao;

    @Column(name = "efeitos_aplicados_em")
    private LocalDateTime efeitosAplicadosEm;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data")
    private List<ProcessoDisciplinarActoEntity> actos = new ArrayList<>();
}
