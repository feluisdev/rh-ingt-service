package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import cv.igrp.RH_Service.shared.application.constants.GrauParentesco;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import java.util.List;


@Getter
@Setter
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dependente")
public class DependenteEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

  
    @Column(name="external_id")
    private UUID externalId;

  
    @NotBlank(message = "nome is mandatory")
    @Column(name="nome", nullable = false)
    private String nome;

  
    @Column(name="data_nascimento")
    private LocalDate dataNascimento;

  
    @Enumerated(EnumType.STRING)
    @Column(name="parentesco")
    private GrauParentesco parentesco;

  
    @Column(name="cpf")
    private String cpf;

  
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_funcionario")
   private FuncionarioEntity idFuncionario;


}