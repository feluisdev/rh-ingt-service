/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsDadosBancariosEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dados_bancarios")
public class DadosBancariosEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "banco", length = 50)
    private String banco;

    @Column(name = "numero_conta", length = 50)
    private String numeroConta;

    @Column(name = "iban", length = 34)
    private String iban;

    @Column(name = "numero_seguranca_social", length = 30)
    private String numeroSegurancaSocial;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
