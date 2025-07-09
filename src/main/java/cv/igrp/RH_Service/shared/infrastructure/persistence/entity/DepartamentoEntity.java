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
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_departamento")
public class DepartamentoEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

  
    @Column(name="external_id")
    private UUID externalId;

  
    @NotBlank(message = "nome is mandatory")
    @Column(name="nome", nullable = false)
    private String nome;

  
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