package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Agendador diário que fecha automaticamente todo o {@link PaaSubmissionPeriod} ainda
 * {@code OPEN} cujo {@code endDate} já passou (PRZ-02).
 * <p>
 * <b>Porque um agendador próprio, e não um método a mais num dos três existentes.</b> Nenhum
 * dos três agendadores diários (`TacitAcceptanceScheduler`, {@code SelfEvaluationOpeningScheduler}
 * e {@code SelfEvaluationTacitAcceptanceScheduler}) fecha períodos de submissão — todos consomem
 * o predicado de janela aberta/fechada, nenhum o escreve. Juntar este fecho a qualquer um deles
 * dar-lhe-ia uma segunda responsabilidade e um nome que deixaria de o descrever.
 * <p>
 * <b>Porque escrever {@code CLOSED} não muda nada para quem já consome a janela por data.</b>
 * {@code PaaSubmissionPeriodRepositoryImpl.findActiveByTypeAndYearAndPurpose} já calcula
 * {@code LocalDate.now(AppTimeZone.CABO_VERDE)} e exige {@code endDate} no futuro ou hoje para
 * considerar um período activo — um período com {@code endDate} no passado já se lê como
 * inactivo, seja qual for o {@code status}. Este agendador torna esse estado explícito na base;
 * não redefine o predicado de {@code SelfEvaluationWindowPolicy#isOpenFor}, que continua
 * byte a byte o mesmo.
 * <p>
 * <b>Corre às 00:30</b>, meia hora antes dos três agendadores das 01:00, para que o estado dos
 * períodos esteja assente antes de eles correrem — hoje nenhum deles depende deste fecho, mas
 * ordenar não custa nada e elimina uma corrida sem razão para existir.
 */
@Component
public class PaaSubmissionPeriodExpiryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaaSubmissionPeriodExpiryScheduler.class);
    private static final int BATCH_SIZE = 100;

    /**
     * Nome do autor de sistema que assina, via {@link SystemAuditor#runAs(String, Runnable)},
     * todas as escritas deste varrimento — público para que o teste possa referi-lo sem
     * duplicar a cadeia literal.
     */
    public static final String AUDITOR_NAME = "scheduler:period-expiry";

    private final PaaSubmissionPeriodRepository repository;

    public PaaSubmissionPeriodExpiryScheduler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    /**
     * Corre diariamente às 00:30 por omissão, sobreponível por
     * {@code sigdi.paa.submission-period-expiry.cron} (variável de ambiente
     * {@code PAA_SUBMISSION_PERIOD_EXPIRY_CRON}). Fecha todos os {@code purpose} e ambos os
     * {@link cv.igrp.RH_Service.sigdi.application.constants.PaaLevel} — é higiene de estado
     * uniforme, sem excepção a explicar depois.
     * <p>
     * <b>Deliberadamente sem {@code @Transactional}, ao contrário dos três agendadores
     * vizinhos.</b> Duas razões, e não esquecimento:
     * <ol>
     *   <li>{@code PaaSubmissionPeriodRepositoryImpl.save} já é {@code @Transactional} e
     *   resolve para {@code merge()} porque o {@code id} chega sempre preenchido — cada período
     *   confirma na sua própria transacção, e a falha num não arrasta os restantes.</li>
     *   <li>Decisiva: o {@code AuditingEntityListener} só resolve o autor no {@code flush}. Com
     *   uma transacção única a envolver o método, esse {@code flush} só ocorreria no
     *   {@code commit}, no fim — <b>depois</b> de o {@code finally} de
     *   {@link SystemAuditor#runAs(String, Runnable)} já ter reposto o {@code ThreadLocal}, e o
     *   autor gravado seria o fallback genérico e não {@link #AUDITOR_NAME}. Sem
     *   {@code @Transactional} no método agendado, o {@code save} por item confirma (logo faz
     *   {@code flush}, logo resolve o autor) ainda dentro do âmbito de {@code runAs}.</li>
     * </ol>
     * <p>
     * O varrimento repete sempre a primeira página do repositório — nunca um índice de página
     * crescente — porque cada período fechado sai do conjunto que a consulta
     * {@code findOpenExpired} devolve; um {@code offset} crescente saltaria linhas por processar.
     * A passagem sai quando o lote vier incompleto ou quando uma passagem inteira não fechar
     * nada — esta segunda condição é o que impede um ciclo infinito quando todos os períodos de
     * um lote falham a gravar.
     */
    @Scheduled(cron = "${sigdi.paa.submission-period-expiry.cron:0 30 0 * * ?}")
    public void closeExpiredPeriods() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        SystemAuditor.runAs(AUDITOR_NAME, () -> scanAndCloseExpiredPeriods(today));
    }

    private void scanAndCloseExpiredPeriods(LocalDate today) {
        int closed = 0;
        int failed = 0;
        int skipped = 0;

        List<PaaSubmissionPeriod> batch;
        boolean closedAnyThisPass;
        do {
            batch = repository.findOpenExpired(today, BATCH_SIZE);
            closedAnyThisPass = false;

            for (PaaSubmissionPeriod period : batch) {
                try {
                    // Segunda verificação da fronteira ontem/hoje, independente da consulta: se
                    // alguma vez consulta e guarda discordarem, o período fica por fechar e o
                    // aviso no fim é o alarme -- nunca se engole em silêncio.
                    if (!period.getEndDate().isBefore(today)) {
                        skipped++;
                        continue;
                    }
                    PaaSubmissionPeriod closedPeriod = period.close();
                    repository.save(closedPeriod);
                    closed++;
                    closedAnyThisPass = true;
                    LOGGER.info("Closed expired PaaSubmissionPeriod ID: {}", period.getId());
                } catch (Exception e) {
                    failed++;
                    LOGGER.error("Failed to close expired PaaSubmissionPeriod ID: {}", period.getId(), e);
                }
            }
            // Sai quando o lote vier incompleto (não há mais para ler) ou quando uma passagem
            // inteira não fechou nada (todos os itens do lote falharam a gravar) -- sem esta
            // segunda condição, um lote cheio que falhe sempre repetiria a mesma página
            // indefinidamente.
        } while (batch.size() == BATCH_SIZE && closedAnyThisPass);

        LOGGER.info("Finished period-expiry closing job. {} period(s) closed, {} failure(s).", closed, failed);
        if (skipped > 0) {
            LOGGER.warn("{} period(s) skipped: query and date guard disagreed on expiry.", skipped);
        }
    }
}
