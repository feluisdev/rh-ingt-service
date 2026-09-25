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
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuração persistida de um job agendado — genérica, serve qualquer {@code ScheduledJob} de
 * qualquer módulo. É a fonte de verdade do agendamento em runtime: o {@code SchedulerService}
 * semeia-a no arranque a partir do {@code getCronPadrao()} do job e, a partir daí, quem manda é
 * esta linha (o cron do código passa a ser só o valor inicial).
 *
 * <p>{@code proximaExecucao} não é um mero adorno de UI: é o que permite detectar que uma execução
 * agendada <em>não aconteceu</em> — ver {@code SchedulerSweeper}. E a própria linha é o lock entre
 * réplicas: o registo de uma execução começa por a bloquear ({@code SELECT … FOR UPDATE}).
 *
 * <p>Tabela nova, sem migração: nasce pelo {@code ddl-auto}, com os índices declarados aqui.
 */
@Getter
@Setter
@IgrpEntity
@Entity
@Audited
@NoArgsConstructor
@Table(name = "t_scheduler_job",
        indexes = @Index(name = "ix_scheduler_job_activo_proxima", columnList = "activo, proxima_execucao"))
public class SchedulerJobEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "chave", nullable = false, unique = true, length = 100)
    private String chave;

    @Column(name = "nome")
    private String nome;

    @Column(name = "cron", nullable = false, length = 100)
    private String cron;

    /** Código de {@code FrequenciaScheduler}; serve de dica ao parsing reverso do cron. */
    @Column(name = "frequencia", length = 20)
    private String frequencia;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    /** Pausa o job sem perder a configuração. Job inactivo não é agendado nem gera omissões. */
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "max_tentativas", nullable = false)
    private Integer maxTentativas;

    @Column(name = "timeout_segundos", nullable = false)
    private Integer timeoutSegundos;

    // Bookkeeping de runtime (muda a cada disparo/sweeper): fora da auditoria para o histórico Envers
    // reter só as alterações de CONFIGURAÇÃO (cron/frequência/timezone/activo), não o ruído das execuções.
    @NotAudited
    @Column(name = "ultima_execucao")
    private LocalDateTime ultimaExecucao;

    @NotAudited
    @Column(name = "ultimo_estado", length = 20)
    private String ultimoEstado;

    /** Instante do próximo disparo agendado. Avançado apenas por execuções AGENDADO e pelo sweeper. */
    @NotAudited
    @Column(name = "proxima_execucao")
    private LocalDateTime proximaExecucao;

    @Column(name = "descricao")
    private String descricao;
}
