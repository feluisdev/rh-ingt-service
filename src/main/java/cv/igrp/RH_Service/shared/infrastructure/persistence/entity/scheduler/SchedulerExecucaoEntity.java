package cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Registo de execução de um job agendado (genérico — qualquer {@code ScheduledJob}, qualquer módulo).
 * Alimentado pelo {@code JobRunner}, responde a "correu? quando? produziu o quê? falhou porquê?".
 *
 * <p>Não é auditado pelo Envers: é ele próprio um registo histórico, e cada linha só muda duas vezes
 * (abertura e fecho). Tabela nova, sem migração: nasce pelo {@code ddl-auto}.
 */
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@Table(name = "t_scheduler_execucao", indexes = {
        @Index(name = "ix_scheduler_execucao_chave_inicio", columnList = "chave, inicio"),
        @Index(name = "ix_scheduler_execucao_chave_estado", columnList = "chave, estado"),
        @Index(name = "ix_scheduler_execucao_chave_agendado", columnList = "chave, agendado_para")
})
public class SchedulerExecucaoEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "chave", nullable = false, length = 100)
    private String chave;

    @Column(name = "nome")
    private String nome;

    @Column(name = "cron", length = 100)
    private String cron;

    @Column(name = "disparo", nullable = false, length = 20)
    private String disparo;

    @Column(name = "solicitante")
    private String solicitante;

    /** Réplica que correu a execução (hostname do pod). */
    @Column(name = "instancia")
    private String instancia;

    /**
     * Instante do cron a que esta execução corresponde. Para execuções AGENDADO é a hora prevista
     * (não a hora a que de facto arrancou); para MANUAL é o momento do pedido. É daqui que o
     * {@code JobContext} deriva o período por omissão — nunca de {@code now()} — para que uma
     * execução tardia continue a processar o período correcto.
     */
    @Column(name = "agendado_para")
    private LocalDateTime agendadoPara;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fim")
    private LocalDateTime fim;

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "erro", columnDefinition = "TEXT")
    private String erro;

    @Column(name = "processados")
    private Integer processados;

    @Column(name = "criados")
    private Integer criados;

    @Column(name = "repetidos")
    private Integer repetidos;

    @Column(name = "saltados")
    private Integer saltados;

    @Column(name = "falhas")
    private Integer falhas;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "mensagem", columnDefinition = "TEXT")
    private String mensagem;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalhes", columnDefinition = "jsonb")
    private Map<String, Object> detalhes;

    /**
     * Parâmetros com que a execução correu. Gravado no ARRANQUE, não no fim: uma execução que
     * rebenta com excepção não produz {@code JobResult}, e sem isto ficaria sem se saber a que
     * período dizia respeito. É também a base do {@code reexecutar}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parametros", columnDefinition = "jsonb")
    private Map<String, Object> parametros;

    /** Número da tentativa dentro da mesma origem (1 = primeira). */
    @Column(name = "tentativa")
    private Integer tentativa;

    /** Execução que deu origem a esta (retry automático ou re-execução manual). */
    @Column(name = "execucao_pai_id")
    private UUID execucaoPaiId;
}
