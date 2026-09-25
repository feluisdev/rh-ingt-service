package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/** Provimento (Lei n.º 20/X/2023, arts. 52.º–78.º): a forma de vínculo, o despacho e a posse. Tabela nova, ddl-auto. */
@Audited
@Getter
@Setter
@IgrpEntity
@Entity(name = "ColabsProvimentoEntity")
@NoArgsConstructor
@Table(name = "t_provimento", indexes = @Index(name = "ix_provimento_funcionario", columnList = "funcionario_id, data_posse"))
public class ProvimentoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private FuncionarioEntity funcionario;

    @Column(name = "modalidade", nullable = false, length = 30)
    private String modalidade;

    @Column(name = "despacho_numero", length = 100)
    private String despachoNumero;

    @Column(name = "despacho_data")
    private LocalDate despachoData;

    @Column(name = "data_posse", nullable = false)
    private LocalDate dataPosse;

    @Column(name = "concurso_ref", length = 100)
    private String concursoRef;

    @Column(name = "vem_de_outra_carreira", nullable = false)
    private Boolean vemDeOutraCarreira;

    /** O período de prova (sem FK física: as duas tabelas referem-se uma à outra). */
    @Column(name = "periodo_prova_id")
    private UUID periodoProvaId;

    /** O provimento de que este é a continuação (sem FK física). */
    @Column(name = "anterior_id")
    private UUID anteriorId;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;
}
