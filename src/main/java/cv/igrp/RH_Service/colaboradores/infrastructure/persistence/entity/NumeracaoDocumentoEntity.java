package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * O último número dado em cada série e ano ({@code DEC-2026}, {@code CIP-2026}…). A linha tranca-se
 * ({@code SELECT … FOR UPDATE}) ao reservar o número seguinte. Não é auditada (é um contador).
 * Tabela nova, criada pelo ddl-auto — sem migração.
 */
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsNumeracaoDocumentoEntity")
@NoArgsConstructor
@Table(name = "t_numeracao_documento")
public class NumeracaoDocumentoEntity {

    /** A série e o ano: {@code DEC-2026}. */
    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "serie", nullable = false, length = 10)
    private String serie;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "ultimo", nullable = false)
    private Integer ultimo;
}
