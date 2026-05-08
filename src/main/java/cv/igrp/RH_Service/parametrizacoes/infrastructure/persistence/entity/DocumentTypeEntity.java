package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_tipo_documento")
public class DocumentTypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "allowed_extensions", length = 200)
    private String allowedExtensions;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_active")
    private Boolean isActive;
}
