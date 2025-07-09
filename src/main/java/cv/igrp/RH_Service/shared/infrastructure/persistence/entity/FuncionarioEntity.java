package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import cv.igrp.RH_Service.shared.application.constants.Estado;


@Getter
@Setter
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_funcionario")
public class FuncionarioEntity extends AuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Integer id;


  @NotNull(message = "externalId is mandatory")
  @Column(name = "external_id", nullable = false)
  private UUID externalId;


  @Column(name = "nome")
  private String nome;


  @Column(name = "nif", unique = true)
  private String nif;


  @Column(name = "num_segurado")
  private String numSegurado;


  @Column(name = "nib")
  private String nib;


  @Column(name = "email", unique = true)
  private String email;


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private List<DependenteEntity> dependentes;


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY)
  private List<QualificacaoEntity> qualificacoes;


  @OneToMany(mappedBy = "responsavelId", fetch = FetchType.LAZY)
  private List<DepartamentoEntity> departamentosresponsaveis;


  @OneToMany(mappedBy = "idFuncionario", fetch = FetchType.LAZY)
  private List<ContratoEntity> contratos;


  @Enumerated(EnumType.STRING)
  @Column(name = "estado")
  private Estado estado;


}
