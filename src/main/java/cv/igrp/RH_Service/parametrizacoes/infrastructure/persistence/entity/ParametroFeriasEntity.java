package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

/** Tabela nova: nasce pelo ddl-auto, sem migração. As datas guardam-se em texto {@code MM-dd}. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_parametro_ferias")
public class ParametroFeriasEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "vigente_desde", unique = true, nullable = false)
    private Integer vigenteDesde;

    @Column(name = "prazo_preferencia", nullable = false, length = 5)
    private String prazoPreferencia;

    @Column(name = "prazo_mapa", nullable = false, length = 5)
    private String prazoMapa;

    @Column(name = "fixacao_inicio", nullable = false, length = 5)
    private String fixacaoInicio;

    @Column(name = "fixacao_fim", nullable = false, length = 5)
    private String fixacaoFim;

    @Column(name = "periodo_minimo_interpolado", nullable = false)
    private Integer periodoMinimoInterpolado;

    @Column(name = "fundamento", length = 255)
    private String fundamento;
}
