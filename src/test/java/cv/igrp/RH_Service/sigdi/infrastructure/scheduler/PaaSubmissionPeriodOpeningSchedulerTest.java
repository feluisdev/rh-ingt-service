package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.service.PeriodFormGenerationService;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Quinze testes Mockito puro, sem Spring e sem base de dados, moldados em
 * {@code PaaSubmissionPeriodExpirySchedulerTest} (Fase 117). Os interruptores
 * {@code enabled} e {@code dryRun}, lidos por {@code @Value} no agendador real, injectam-se aqui
 * por reflexão sobre os campos ({@link ReflectionTestUtils#setField}), porque este teste não
 * levanta o contexto Spring e portanto {@code @Value} nunca é resolvido.
 * <p>
 * As datas dos fixtures constroem-se relativamente a {@code LocalDate.now(AppTimeZone.CABO_VERDE)},
 * a mesma expressão que o próprio agendador usa, para que o teste nunca dependa do fuso horário
 * da máquina onde corre.
 */
@ExtendWith(MockitoExtension.class)
class PaaSubmissionPeriodOpeningSchedulerTest {

    private static final int BATCH_SIZE = 50;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @Mock
    private FormGenerationBatchRepository formGenerationBatchRepository;

    @Mock
    private PeriodFormGenerationService periodFormGenerationService;

    @InjectMocks
    private PaaSubmissionPeriodOpeningScheduler scheduler;

    private PaaSubmissionPeriod buildActivePeriod(LocalDate today) {
        return PaaSubmissionPeriod.reconstruct(UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.minusDays(5), today.plusDays(5), "OPEN", today.getYear());
    }

    private FormGenerationBatch buildBatch(UUID periodId, FormGenerationBatchStatus status) {
        FormGenerationBatch batch = FormGenerationBatch.start(periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                2026, FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", LocalDateTime.now());
        // finish() deriva o status a partir dos contadores -- para um teste de idempotência só
        // interessa o status devolvido por findByPeriodId, não como se chegou lá, por isso os
        // lotes-fixture aqui usam reconstruct diretamente com o status já fixado.
        return FormGenerationBatch.reconstruct(batch.getId(), periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                2026, FormGenerationBatch.CREATES_FORMS, false, status, 0, 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(), "scheduler:period-opening", List.of());
    }

    @Test
    void aPeriodWithNoBatchIsPassedToTheGenerationService() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId())).thenReturn(List.of());
        when(periodFormGenerationService.generateFor(any(), anyBooleanDryRun()))
                .thenReturn(buildBatch(period.getId(), FormGenerationBatchStatus.COMPLETED));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, times(1)).generateFor(eq(period), eq(false));
    }

    @Test
    void aPeriodWithACompletedBatchIsSkipped() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.COMPLETED)));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    @Test
    void aPeriodWithANothingToGenerateBatchIsSkipped() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.NOTHING_TO_GENERATE)));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    @Test
    void aPeriodWithAFailedBatchIsSkipped() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.FAILED)));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    /**
     * Fase 120, plano 06 -- reproducao em teste do defeito medido no plano 05 contra base real:
     * um minuto depois de uma reversao o agendador recriou a avaliacao apagada. Desfazer e um
     * acto deliberado; regenerar anula-lo-ia sem ninguem pedir.
     */
    @Test
    void aPeriodWithARevertedBatchIsNotGeneratedAgain() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.REVERTED)));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    /**
     * O mesmo para a reversao parcial: as avaliacoes que ja tinham avancado de fase ficaram, mas
     * as apagadas nao podem voltar. Um lote parcialmente desfeito tambem nao se retoma.
     */
    @Test
    void aPeriodWithAPartiallyRevertedBatchIsNotGeneratedAgain() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.PARTIALLY_REVERTED)));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    @Test
    void aPeriodWithAPartialBatchIsGeneratedAgain() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.PARTIAL)));
        when(periodFormGenerationService.generateFor(any(), anyBooleanDryRun()))
                .thenReturn(buildBatch(period.getId(), FormGenerationBatchStatus.COMPLETED));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, times(1)).generateFor(eq(period), eq(false));
    }

    @Test
    void aPeriodWithADryRunBatchIsGenerated() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId()))
                .thenReturn(List.of(buildBatch(period.getId(), FormGenerationBatchStatus.DRY_RUN)));
        when(periodFormGenerationService.generateFor(any(), anyBooleanDryRun()))
                .thenReturn(buildBatch(period.getId(), FormGenerationBatchStatus.COMPLETED));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, times(1)).generateFor(eq(period), eq(false));
    }

    @Test
    void theSystemAuditorIsActiveDuringTheServiceCallAndClearedAfter() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId())).thenReturn(List.of());

        AtomicReference<Optional<String>> auditorDuringCall = new AtomicReference<>();
        when(periodFormGenerationService.generateFor(any(), anyBooleanDryRun()))
                .thenAnswer(invocation -> {
                    auditorDuringCall.set(SystemAuditor.current());
                    return buildBatch(period.getId(), FormGenerationBatchStatus.COMPLETED);
                });

        scheduler.generateFormsForOpenPeriods();

        assertEquals(Optional.of(PaaSubmissionPeriodOpeningScheduler.AUDITOR_NAME), auditorDuringCall.get());
        assertTrue(SystemAuditor.current().isEmpty());
    }

    @Test
    void anExceptionGeneratingOnePeriodDoesNotStopTheOthers() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod first = buildActivePeriod(today);
        PaaSubmissionPeriod second = buildActivePeriod(today);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(first, second));
        when(formGenerationBatchRepository.findByPeriodId(any())).thenReturn(List.of());
        when(periodFormGenerationService.generateFor(eq(first), anyBooleanDryRun()))
                .thenThrow(new RuntimeException("falha simulada de geracao"));
        when(periodFormGenerationService.generateFor(eq(second), anyBooleanDryRun()))
                .thenReturn(buildBatch(second.getId(), FormGenerationBatchStatus.COMPLETED));

        assertDoesNotThrow(() -> scheduler.generateFormsForOpenPeriods());

        verify(periodFormGenerationService, times(1)).generateFor(eq(first), anyBooleanDryRun());
        verify(periodFormGenerationService, times(1)).generateFor(eq(second), anyBooleanDryRun());
    }

    @Test
    void whenDisabledFindOpenActiveOnIsNeverCalled() {
        ReflectionTestUtils.setField(scheduler, "enabled", false);

        scheduler.generateFormsForOpenPeriods();

        verify(periodRepository, never()).findOpenActiveOn(any(), anyInt());
        verify(periodFormGenerationService, never()).generateFor(any(), anyBoolean());
    }

    @Test
    void whenDryRunTheServiceIsCalledWithTrue() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod period = buildActivePeriod(today);
        ReflectionTestUtils.setField(scheduler, "dryRun", true);

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(period));
        when(formGenerationBatchRepository.findByPeriodId(period.getId())).thenReturn(List.of());
        when(periodFormGenerationService.generateFor(any(), eq(true)))
                .thenReturn(buildBatch(period.getId(), FormGenerationBatchStatus.DRY_RUN));

        scheduler.generateFormsForOpenPeriods();

        verify(periodFormGenerationService, times(1)).generateFor(eq(period), eq(true));
    }

    @Test
    void scansInBatchesAndStopsWhenAWholePassGeneratesNothing() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        List<PaaSubmissionPeriod> batch = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            batch.add(buildActivePeriod(today));
        }

        when(periodRepository.findOpenActiveOn(eq(today), eq(BATCH_SIZE))).thenReturn(batch);
        when(formGenerationBatchRepository.findByPeriodId(any())).thenReturn(List.of());
        when(periodFormGenerationService.generateFor(any(), anyBooleanDryRun()))
                .thenThrow(new RuntimeException("falha simulada de geracao"));

        scheduler.generateFormsForOpenPeriods();

        verify(periodRepository, times(1)).findOpenActiveOn(eq(today), eq(BATCH_SIZE));
    }

    @Test
    void scheduledCronDefaultsToQuarterToOneAndStaysOverridable() throws NoSuchMethodException {
        Method method = PaaSubmissionPeriodOpeningScheduler.class.getMethod("generateFormsForOpenPeriods");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertEquals("${sigdi.paa.form-generation.cron:0 45 0 * * ?}", scheduled.cron());
    }

    /**
     * Legenda da garantia, não a garantia. Quem impede que um estado novo do enum fique por
     * classificar é o compilador: {@code blocksRegeneration} é um {@code switch} de expressão sem
     * ramo {@code default}, e uma constante por tratar quebra a compilação. Este teste existe
     * para que isso fique dito a quem ler, e para apanhar o caso de alguém reintroduzir um
     * {@code default} -- aí o compilador cala-se e só a contagem denuncia.
     *
     * <p>Foi a ausência desta obrigação que produziu o defeito medido no plano 05: o plano 01
     * acrescentou dois estados ao enum e a classificação, então um {@code Set.of}, não os
     * conhecia.
     */
    @Test
    void everyBatchStatusIsExplicitlyClassifiedForRegeneration() {
        int classified = 0;
        for (FormGenerationBatchStatus status : FormGenerationBatchStatus.values()) {
            final FormGenerationBatchStatus current = status;
            assertDoesNotThrow(() -> PaaSubmissionPeriodOpeningScheduler.blocksRegeneration(current),
                    "Estado sem classificacao de regeneracao: " + current);
            classified++;
        }

        assertEquals(FormGenerationBatchStatus.values().length, classified,
                "Todos os estados do enum tem de passar pela classificacao");
        assertTrue(PaaSubmissionPeriodOpeningScheduler.blocksRegeneration(FormGenerationBatchStatus.REVERTED),
                "Um lote desfeito nao pode voltar a ser gerado");
        assertTrue(PaaSubmissionPeriodOpeningScheduler.blocksRegeneration(FormGenerationBatchStatus.PARTIALLY_REVERTED),
                "Um lote parcialmente desfeito nao pode voltar a ser gerado");
    }

    // Helpers de legibilidade -- evitam repetir any(Boolean.class) / anyBoolean() com o mesmo
    // significado em todo o ficheiro.
    private static boolean anyBoolean() {
        return org.mockito.ArgumentMatchers.anyBoolean();
    }

    private static boolean anyBooleanDryRun() {
        return org.mockito.ArgumentMatchers.anyBoolean();
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
