/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.math.BigDecimal;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import java.util.List;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_qualificacao")
public class QualificacaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "instituicao is mandatory")
    @Column(name="instituicao", nullable = false)
    private String instituicao;

  
    @Column(name="curso")
    private String curso;

  
    @Column(name="data_inicio")
    private LocalDate dataInicio;

  
    @Column(name="data_conclusao")
    private LocalDate dataConclusao;

  
    @Column(name="nivel")
    private String nivel;

  
    @Column(name="situacao")
    private String situacao;

  
    @Column(name="carga_horaria")
    private Integer cargaHoraria;

  
    @Column(name="notafinal")
    private BigDecimal notaFinal;

  
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_funcionario")
   private FuncionarioEntity idFuncionario;


}