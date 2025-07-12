package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.time.LocalDate;
import java.math.BigDecimal;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import java.util.List;


@Getter
@Setter
@ToString
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_contrato_entity")
public class ContratoEntity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

  
    @NotNull(message = "externalId is mandatory")
    @Column(name="external_id", nullable = false)
    private UUID externalId;

  
    @Column(name="tipo_contrato")
    private String tipoContrato;

  
    @Column(name="data_inicio")
    private LocalDate dataInicio;

  
    @Column(name="data_fim")
    private LocalDate dataFim;

  
    @Column(name="salario")
    private BigDecimal salario;

  
    @Column(name="carga_horaria")
    private Integer cargaHoraria;

  
    @Column(name="observacoes")
    private String observacoes;

  
    @Enumerated(EnumType.STRING)
    @Column(name="estado")
    private Estado estado;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_departamento")
   private DepartamentoEntity idDepartamento;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_funcionario")
   private FuncionarioEntity idFuncionario;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_cargo")
   private CargoEntity idCargo;


}