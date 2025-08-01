/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import java.util.List;


@Getter
@Setter
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_documento")
public class DocumentoEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @Column(name="url")
    private String url;

  
    @Column(name="observacao")
    private String observacao;

  
    @Enumerated(EnumType.STRING)
    @Column(name="objecto_tipo")
    private ObjetoTipo objectoTipo;

  
    @Column(name="object_id")
    private UUID objectId;

  
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_tipo_doc")
   private TipoDocumentoEntity idTipoDoc;


}