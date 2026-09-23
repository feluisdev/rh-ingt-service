package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Quando se deu conhecimento do mapa de ferias do ano (art. 6.o n.o 1). Um por ano. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsMapaFeriasEntity")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_ferias_mapa")
public class MapaFeriasEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "ano", nullable = false, unique = true)
    private int ano;

    @Column(name = "publicado_em", nullable = false)
    private LocalDate publicadoEm;
}
