package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.service.PeriodFormGenerationService;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Agendador diário que despoleta a geração automática de formulários (Fase 119, {@code PRZ-01})
 * assim que um {@link PaaSubmissionPeriod} começa a decorrer. O agregado não tem evento de
 * abertura -- nasce {@code OPEN} com um {@code startDate} que pode estar no futuro; "abertura",
 * para efeitos do {@code PRZ-01}, é portanto o primeiro dia em que o período está a decorrer e
 * ainda não tem lote (ver {@code PaaSubmissionPeriodEntityRepository.findOpenActiveOn}).
 * <p>
 * <b>Porque um agendador próprio, o quarto do módulo (D-17).</b> Nenhum dos três agendadores
 * diários já existentes (incluindo {@link cv.igrp.RH_Service.sigdi.infrastructure.scheduler.PaaSubmissionPeriodExpiryScheduler}
 * da Fase 117) cria agregados -- todos transicionam. Juntar a criação a um deles dar-lhe-ia uma
 * segunda responsabilidade e um nome que deixaria de o descrever -- a mesma razão escrita no
 * Javadoc do agendador de fecho.
 * <p>
 * <b>Corre às 00:45 (D-18)</b>, entre o fecho por expiração (00:30) e os três agendadores das
 * 01:00. A ordem importa: um período que expirou esta noite tem de ficar {@code CLOSED} antes de
 * este varrimento correr, senão gera-se para um prazo que já acabou -- o fecho corre primeiro e o
 * finder exige {@code status = OPEN}, que é a segunda linha de defesa.
 * <p>
 * <b>{@link #AUDITOR_NAME} distinto do autor usado pelo agendador de fecho (D-19).</b> Dois
 * agendadores a assinar com o mesmo nome de autor tornariam indistinguível, na auditoria, qual
 * dos dois escreveu o quê -- por isso este agendador tem o seu próprio {@link #AUDITOR_NAME},
 * literalmente diferente do usado por {@link PaaSubmissionPeriodExpiryScheduler#AUDITOR_NAME}.
 * <p>
 * <b>Deliberadamente sem {@code @Transactional} no método agendado (D-20).</b> Não é
 * esquecimento -- é a decisão da Fase 117, provada contra base real e transcrita no
 * {@code OBSERVACAO-BASE-REAL.md} dessa fase: o {@code AuditingEntityListener} só resolve o
 * autor no {@code flush}. Com uma transacção única a envolver este método, esse {@code flush} só
 * ocorreria no {@code commit}, no fim -- <b>depois</b> de o {@code finally} de
 * {@link SystemAuditor#runAs(String, Runnable)} já ter reposto o {@code ThreadLocal}, e o autor
 * gravado seria o fallback genérico, não {@link #AUDITOR_NAME}. {@link PeriodFormGenerationService#generateFor}
 * já não é {@code @Transactional} pela mesma razão (ver o seu próprio Javadoc); acrescentar
 * {@code @Transactional} aqui, no chamador, reintroduziria exactamente o problema que essa
 * decisão evita. <b>Quem acrescentar {@code @Transactional} aqui desfaz uma observação provada
 * contra base real.</b>
 */
@Component
public class PaaSubmissionPeriodOpeningScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaaSubmissionPeriodOpeningScheduler.class);
    private static final int BATCH_SIZE = 50;

    /**
     * Nome do autor de sistema que assina, via {@link SystemAuditor#runAs(String, Runnable)},
     * todas as escritas deste varrimento -- público para que o teste e o plano 07 lhe possam
     * chamar pelo nome sem duplicar a cadeia literal.
     */
    public static final String AUDITOR_NAME = "scheduler:period-opening";

    /**
     * Decide se um lote já existente bloqueia nova geração para o mesmo período -- a guarda de
     * idempotência (D-21).
     *
     * <p>Não bloqueiam, de propósito: {@code PARTIAL}, que é justamente o caso que se quer
     * retomar (os itens já criados resolvem-se como {@code ALREADY_EXISTED} no gerador, plano 03
     * da Fase 119), e {@code DRY_RUN}, porque uma simulação não pode impedir a geração a sério.
     *
     * <p>Bloqueiam {@code REVERTED} e {@code PARTIALLY_REVERTED}: desfazer é o acto deliberado de
     * dizer que aquelas avaliações não deviam ter existido, e regenerá-las no dia seguinte
     * anularia a reversão sem ninguém pedir e sem aparecer a ninguém. Quem quiser gerar depois de
     * desfazer gera à mão -- a capacidade de criar avaliações já existe. Geração a pedido é o
     * {@code PRZ-08}, diferido para v2.
     *
     * <p><strong>Porque isto é um {@code switch} de expressão sem {@code default}, e tem de
     * continuar a ser:</strong> até à Fase 120 esta classificação era um {@code Set.of} com as
     * cinco constantes que o enum tinha quando o plano 04 da Fase 119 o escreveu. O plano 01
     * desta fase acrescentou {@code REVERTED} e {@code PARTIALLY_REVERTED} ao enum e nada obrigou
     * a classificá-los aqui -- um {@code Set} aceita em silêncio qualquer subconjunto. O
     * resultado foi medido contra base real no plano 05: um minuto depois de uma reversão o
     * agendador recriou a avaliação apagada. Um {@code switch} de expressão sobre um enum sem
     * ramo {@code default} <em>não compila</em> enquanto houver uma constante por tratar, e é
     * essa a única razão de esta forma ter sido escolhida. Acrescentar um {@code default} devolve
     * o defeito.
     *
     * <p>Acesso de pacote, e não privado, para que
     * {@code PaaSubmissionPeriodOpeningSchedulerTest} -- no mesmo pacote -- possa percorrer
     * {@code values()} e documentar a exaustividade sem reflexão.
     */
    static boolean blocksRegeneration(FormGenerationBatchStatus status) {
        return switch (status) {
            case COMPLETED, NOTHING_TO_GENERATE, FAILED, REVERTED, PARTIALLY_REVERTED -> true;
            case PARTIAL, DRY_RUN -> false;
        };
    }

    private final PaaSubmissionPeriodRepository periodRepository;
    private final FormGenerationBatchRepository formGenerationBatchRepository;
    private final PeriodFormGenerationService periodFormGenerationService;

    /**
     * Desliga o varrimento por inteiro (D-22). Omissão {@code true} -- sobreponível pela
     * propriedade {@code sigdi.paa.form-generation.enabled}, variável de ambiente
     * {@code PAA_FORM_GENERATION_ENABLED}. Existe porque a criação em massa não tem inverso até
     * à Fase 120: o operador tem de poder parar isto sem tocar em código.
     */
    @Value("${sigdi.paa.form-generation.enabled:true}")
    private boolean enabled = true;

    /**
     * Passa {@code dryRun = true} ao {@link PeriodFormGenerationService} em vez de gerar a sério
     * (D-22). Omissão {@code false} -- sobreponível pela propriedade
     * {@code sigdi.paa.form-generation.dry-run}, variável de ambiente
     * {@code PAA_FORM_GENERATION_DRY_RUN}. Mesma razão do interruptor acima: permite ver o lote
     * que seria criado antes de confiar na geração a sério.
     */
    @Value("${sigdi.paa.form-generation.dry-run:false}")
    private boolean dryRun;

    public PaaSubmissionPeriodOpeningScheduler(PaaSubmissionPeriodRepository periodRepository,
                                                FormGenerationBatchRepository formGenerationBatchRepository,
                                                PeriodFormGenerationService periodFormGenerationService) {
        this.periodRepository = periodRepository;
        this.formGenerationBatchRepository = formGenerationBatchRepository;
        this.periodFormGenerationService = periodFormGenerationService;
    }

    /**
     * Corre diariamente às 00:45 por omissão, sobreponível por
     * {@code sigdi.paa.form-generation.cron} (variável de ambiente {@code PAA_FORM_GENERATION_CRON}).
     * <p>
     * Deliberadamente sem {@code @Transactional} -- ver Javadoc de classe (D-20).
     */
    @Scheduled(cron = "${sigdi.paa.form-generation.cron:0 45 0 * * ?}")
    public void generateFormsForOpenPeriods() {
        if (!enabled) {
            LOGGER.warn("Geracao automatica de formularios desligada por sigdi.paa.form-generation.enabled=false. "
                    + "Nenhum periodo foi processado nesta passagem.");
            return;
        }
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        SystemAuditor.runAs(AUDITOR_NAME, () -> scanAndGenerate(today));
    }

    private void scanAndGenerate(LocalDate today) {
        int generated = 0;
        int skipped = 0;
        int failed = 0;

        List<PaaSubmissionPeriod> batch;
        boolean generatedAnyThisPass;
        do {
            batch = periodRepository.findOpenActiveOn(today, BATCH_SIZE);
            generatedAnyThisPass = false;

            for (PaaSubmissionPeriod period : batch) {
                try {
                    if (hasTerminalBatch(period)) {
                        skipped++;
                        continue;
                    }
                    FormGenerationBatch result = periodFormGenerationService.generateFor(period, dryRun);
                    generated++;
                    generatedAnyThisPass = true;
                    LOGGER.info("Generated forms for PaaSubmissionPeriod ID: {}, batch status: {}",
                            period.getId(), result.getStatus());
                } catch (Exception e) {
                    failed++;
                    LOGGER.error("Failed to generate forms for PaaSubmissionPeriod ID: {}", period.getId(), e);
                }
            }
            // Sai quando o lote vier incompleto (não há mais para ler) ou quando uma passagem
            // inteira não gerou nada (todos os itens do lote falharam ou foram saltados) -- sem
            // esta segunda condição, um lote cheio de períodos que falhem sempre repetiria a
            // mesma página indefinidamente (D-23).
        } while (batch.size() == BATCH_SIZE && generatedAnyThisPass);

        LOGGER.info("Finished period-opening generation job. {} period(s) generated, {} skipped, {} failure(s).",
                generated, skipped, failed);
    }

    private boolean hasTerminalBatch(PaaSubmissionPeriod period) {
        return formGenerationBatchRepository.findByPeriodId(period.getId())
                .stream()
                .anyMatch(existingBatch -> blocksRegeneration(existingBatch.getStatus()));
    }
}
