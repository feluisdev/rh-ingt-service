/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import cv.igrp.RH_Service.shared.application.constants.EstadoCivil;
import cv.igrp.RH_Service.shared.application.constants.Sexo;
import java.util.List;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import cv.igrp.RH_Service.shared.application.constants.Estado;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_funcionario")
public class FuncionarioEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @Column(name="nome")
    private String nome;

  
    @Column(name="nif", unique = true)
    private String nif;

  
    @Column(name="num_segurado")
    private String numSegurado;

  
    @Column(name="nib")
    private String nib;

  
    @Column(name="email", unique = true)
    private String email;

  
    @Column(name="endereco")
    private String endereco;

  
    @Enumerated(EnumType.STRING)
    @Column(name="estado_civil")
    private EstadoCivil estadoCivil;

  
    @Enumerated(EnumType.STRING)
    @Column(name="sexo")
    private Sexo sexo;

  


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
private List<DependenteEntity> dependentes;


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY)
private List<QualificacaoEntity> qualificacoes;


  @OneToMany(mappedBy = "responsavelId", fetch = FetchType.LAZY)
private List<DepartamentoEntity> departamentosresponsaveis;


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY)
private List<ContratoEntity> contratos;
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

  
}