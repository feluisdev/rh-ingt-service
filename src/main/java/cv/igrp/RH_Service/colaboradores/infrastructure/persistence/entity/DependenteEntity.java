/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsDependenteEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dependente")
public class DependenteEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "parentesco", nullable = false, length = 50)
    private String parentesco;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
