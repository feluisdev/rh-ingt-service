/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import cv.igrp.RH_Service.shared.application.constants.Estado;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_departamento")
public class DepartamentoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "nome is mandatory")
    @Column(name="nome", nullable = false)
    private String nome;

  
    @Column(name="codigo", unique = true)
    private String codigo;

  
    @Column(name="descricao")
    private String descricao;

  
    @Column(name="localizacao")
    private String localizacao;

  
    @Column(name="orcamento")
    private BigDecimal orcamento;

  


  @OneToMany(mappedBy = "idDepartamento", fetch = FetchType.LAZY)
private List<ContratoEntity> contratos;
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "responsavel_id")
   private FuncionarioEntity responsavelId;


}