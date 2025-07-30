/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.List;
import cv.igrp.RH_Service.shared.application.constants.Estado;


@Getter
@Setter
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_tipo_documento")
public class TipoDocumentoEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

  
    @NotNull(message = "externalId is mandatory")
    @Column(name="external_id", nullable = false)
    private UUID externalId;

  
    @Column(name="descricao")
    private String descricao;

  
    @Column(name="codigo", unique = true)
    private String codigo;

  


  @OneToMany(mappedBy = "idTipoDoc", fetch = FetchType.LAZY)
private List<DocumentoEntity> documentos;
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

  
}