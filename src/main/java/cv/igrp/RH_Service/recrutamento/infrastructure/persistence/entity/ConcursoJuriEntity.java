package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.UUID;

/** Um membro do júri do concurso. Tabela nova, criada pelo ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "RecrutConcursoJuriEntity")
@NoArgsConstructor
@Table(name = "t_concurso_juri")
public class ConcursoJuriEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concurso_id", nullable = false)
    private ConcursoEntity concurso;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    /** PRESIDENTE, VOGAL, SUPLENTE. */
    @Column(name = "papel", nullable = false, length = 20)
    private String papel;

    @Column(name = "nome", nullable = false, length = 300)
    private String nome;

    /** Quando é funcionário da casa (sem FK física). */
    @Column(name = "funcionario_id")
    private UUID funcionarioId;
}
