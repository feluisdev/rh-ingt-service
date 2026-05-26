package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Audited
@Getter
@Setter
@Table(name = "t_historico_estado_colaborador")
public class HistoricoEstadoColaboradorEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "estado_anterior_id")
    private UUID estadoAnteriorId;

    @Column(name = "estado_novo_id", nullable = false)
    private UUID estadoNovoId;

    @Column(name = "motivo_ckey", length = 100)
    private String motivoCkey;

    @Column(name = "data_efectividade", nullable = false)
    private LocalDate dataEfectividade;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;
}
