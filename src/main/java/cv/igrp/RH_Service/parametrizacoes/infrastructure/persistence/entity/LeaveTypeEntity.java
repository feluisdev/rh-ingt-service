/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */
package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_leave_type")
public class LeaveTypeEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "deducts_balance")
    private Boolean deductsBalance;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(name = "max_days_per_year")
    private Integer maxDaysPerYear;

    /**
     * Limite por ACONTECIMENTO (V53). O art. 15.o n.o 1 do DL n.o 3/2010 quase so fala assim:
     * 6 dias por ocasiao do casamento, 8 por falecimento do conjuge, 2 por cada prova.
     * Nulo quer dizer que a lei nao poe limite desta natureza.
     */
    @Column(name = "max_days_per_occurrence")
    private Integer maxDaysPerOccurrence;

    /** Limite por mes civil (V53): art. 15.o n.o 1 al. o) e al. q). Nulo e sem limite. */
    @Column(name = "max_days_per_month")
    private Integer maxDaysPerMonth;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Regime legal do DL n.o 3/2010: FERIAS (cap. II), FALTA (cap. III, seccao II) ou
     * FALTA_INJUSTIFICADA (seccao III, que nao conta para antiguidade). Ver V49 e V54.
     */
    @Column(name = "regime", length = 20)
    private String regime;

    /**
     * O que a ausencia faz a remuneracao (art. 16.o; V54). Esta aplicacao nao calcula
     * remuneracao -- guarda a classificacao para quem a processa a poder ler.
     */
    @Column(name = "efeito_remuneracao", length = 30)
    private String efeitoRemuneracao;

    /** Como se contam os dias (art. 76.o; V56): DIAS_UTEIS ou DIAS_SEGUIDOS. */
    @Column(name = "contagem", length = 20)
    private String contagem;

    /** V58: tecto diário dos pedidos em horas. Nulo = sem tecto. */
    @Column(name = "max_minutos_por_dia")
    private Integer maxMinutosPorDia;
}
