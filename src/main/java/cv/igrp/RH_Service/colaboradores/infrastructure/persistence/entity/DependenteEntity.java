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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "relationship_type", length = 50)
    private String relationshipType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
