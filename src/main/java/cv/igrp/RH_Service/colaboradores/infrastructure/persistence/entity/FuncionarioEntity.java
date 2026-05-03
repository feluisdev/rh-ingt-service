/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsFuncionarioEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_funcionario")
public class FuncionarioEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "numero_funcionario", unique = true, nullable = false, length = 10, updatable = false)
    private String numeroFuncionario;

    @Column(name = "nome_completo", nullable = false, length = 200)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "genero", nullable = false, length = 50)
    private String genero;

    @Column(name = "estado_civil", nullable = false, length = 50)
    private String estadoCivil;

    @Column(name = "nif", unique = true, nullable = false, length = 20)
    private String nif;

    @Column(name = "bi_numero", unique = true, nullable = false, length = 50)
    private String biNumero;

    @Column(name = "bi_validade")
    private LocalDate biValidade;

    @Column(name = "nacionalidade", nullable = false, length = 50)
    private String nacionalidade;

    @Column(name = "email", unique = true, length = 200)
    private String email;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "morada")
    private String morada;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "situacao_profissional", nullable = false, length = 50)
    private String situacaoProfissional;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
