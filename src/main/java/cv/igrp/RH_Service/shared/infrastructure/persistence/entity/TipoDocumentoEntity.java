/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.util.List;

import cv.igrp.RH_Service.shared.application.constants.Estado;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_tipo_documento")
public class TipoDocumentoEntity extends AuditEntity {

  @Id
  @Column(name = "id", unique = true, nullable = false)
  private UUID id;


  @Column(name = "descricao")
  private String descricao;


  @Column(name = "codigo", unique = true)
  private String codigo;



  @Enumerated(EnumType.STRING)
  @Column(name = "estado")
  private Estado estado;


}
