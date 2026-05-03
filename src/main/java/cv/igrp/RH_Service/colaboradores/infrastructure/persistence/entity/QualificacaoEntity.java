/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsQualificacaoEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_qualificacao")
public class QualificacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "nivel_academico", nullable = false, length = 50)
    private String nivelAcademico;

    @Column(name = "curso", nullable = false, length = 200)
    private String curso;

    @Column(name = "instituicao", length = 200)
    private String instituicao;

    @Column(name = "ano_conclusao")
    private Integer anoConclusao;

    @Column(name = "pais", length = 10)
    private String pais;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
