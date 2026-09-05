/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.DocumentTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.WorkerStateEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentTypeEntity documentType;

    @Column(name = "numero_documento", unique = true, length = 50)
    private String numeroDocumento;

    @Column(name = "data_emissao_doc")
    private LocalDate dataEmissaoDoc;

    @Column(name = "data_validade_doc")
    private LocalDate dataValidadeDoc;

    @Column(name = "nacionalidade", nullable = false, length = 50)
    private String nacionalidade;

    @Column(name = "email", unique = true, length = 200)
    private String email;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "morada")
    private String morada;

    @Column(name = "ilha", length = 100)
    private String ilha;

    @Column(name = "concelho", length = 100)
    private String concelho;

    @Column(name = "localidade", length = 100)
    private String localidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_state_id")
    private WorkerStateEntity workerState;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
