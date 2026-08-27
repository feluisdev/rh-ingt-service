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
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;

import java.lang.reflect.Method;
import java.time.LocalDate;
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

/**
 * Oito testes Mockito puro, sem Spring e sem base de dados, moldados em
 * {@code SelfEvaluationOpeningSchedulerTest}. As datas dos fixtures constroem-se relativamente a
 * {@code LocalDate.now(AppTimeZone.CABO_VERDE)}, a mesma expressão que o próprio agendador usa,
 * para que o teste nunca dependa do fuso horário da máquina onde corre.
 * <p>
 * <b>O que o teste 5 prova, e o que não prova.</b>
 * {@code theSystemAuditorIsActiveWhileSaving} confirma, via {@code thenAnswer} sobre
 * {@code repository.save}, que {@link SystemAuditor#current()} devolve
 * {@link PaaSubmissionPeriodExpiryScheduler#AUDITOR_NAME} exactamente durante a chamada a
 * {@code save}, e que fica vazio depois de o método agendado retornar. Isto prova que o âmbito de
 * auditoria cobre a chamada Java a {@code save} -- **não** prova que cobre o {@code flush} do
 * Hibernate, que só acontece dentro da transacção própria de
 * {@code PaaSubmissionPeriodRepositoryImpl.save} e exige uma base de dados real para observar.
 * Essa observação fica para o plano 03.
 */
@ExtendWith(MockitoExtension.class)
class PaaSubmissionPeriodExpirySchedulerTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private PaaSubmissionPeriodRepository repository;

    @InjectMocks
    private PaaSubmissionPeriodExpiryScheduler scheduler;

    private PaaSubmissionPeriod buildOpenPeriod(Purpose purpose, PaaLevel type, LocalDate endDate) {
        LocalDate startDate = endDate.minusDays(10);
        return PaaSubmissionPeriod.reconstruct(UUID.randomUUID(), purpose, type, startDate, endDate, "OPEN", 2026);
    }

    @Test
    void closesAPeriodThatEndedYesterday() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod expired = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1));

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(expired));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.closeExpiredPeriods();

        ArgumentCaptor<PaaSubmissionPeriod> captor = ArgumentCaptor.forClass(PaaSubmissionPeriod.class);
        verify(repository, times(1)).save(captor.capture());
        PaaSubmissionPeriod saved = captor.getValue();
        assertTrue(saved.isClosed());
        assertEquals(expired.getId(), saved.getId());
        assertEquals(expired.getPurpose(), saved.getPurpose());
        assertEquals(expired.getType(), saved.getType());
        assertEquals(expired.getYear(), saved.getYear());
    }

    @Test
    void leavesOpenAPeriodThatEndsToday() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        // Hipótese adversa: a consulta devolve um período cujo endDate é hoje. A guarda do
        // agendador tem de o rejeitar mesmo assim -- é a prova do critério 3.
        PaaSubmissionPeriod endsToday = buildOpenPeriod(Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL, today);

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(endsToday));

        scheduler.closeExpiredPeriods();

        verify(repository, never()).save(any());
    }

    @Test
    void closesEveryPurposeAndBothLevels() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod p1 = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1));
        PaaSubmissionPeriod p2 = buildOpenPeriod(Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, today.minusDays(2));
        PaaSubmissionPeriod p3 = buildOpenPeriod(Purpose.SIADAP_SELF_EVAL, PaaLevel.UNIT_LEVEL, today.minusDays(3));
        PaaSubmissionPeriod p4 = buildOpenPeriod(Purpose.PAA_BSC_OBJECTIVES, PaaLevel.INDIVIDUAL_LEVEL, today.minusDays(4));

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(p1, p2, p3, p4));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.closeExpiredPeriods();

        verify(repository, times(4)).save(any());
    }

    @Test
    void oneFailureDoesNotStopTheBatch() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod first = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1));
        PaaSubmissionPeriod second = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(2));
        PaaSubmissionPeriod third = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(3));

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE)))
                .thenReturn(List.of(first, second, third));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new RuntimeException("falha simulada de gravação"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> scheduler.closeExpiredPeriods());

        verify(repository, times(3)).save(any());
    }

    @Test
    void theSystemAuditorIsActiveWhileSaving() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        PaaSubmissionPeriod expired = buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1));

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE))).thenReturn(List.of(expired));

        AtomicReference<Optional<String>> auditorDuringSave = new AtomicReference<>();
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> {
                    auditorDuringSave.set(SystemAuditor.current());
                    return invocation.getArgument(0);
                });

        scheduler.closeExpiredPeriods();

        assertEquals(Optional.of(PaaSubmissionPeriodExpiryScheduler.AUDITOR_NAME), auditorDuringSave.get());
        assertTrue(SystemAuditor.current().isEmpty());
    }

    @Test
    void scansInBatchesWithoutSkippingRows() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        List<PaaSubmissionPeriod> firstBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            firstBatch.add(buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1)));
        }
        List<PaaSubmissionPeriod> secondBatch = List.of(
                buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1)));

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE)))
                .thenReturn(firstBatch)
                .thenReturn(secondBatch);
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.closeExpiredPeriods();

        verify(repository, times(101)).save(any());
        verify(repository, times(2)).findOpenExpired(eq(today), eq(BATCH_SIZE));
    }

    @Test
    void stopsWhenAWholePassClosesNothing() {
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        List<PaaSubmissionPeriod> batch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            batch.add(buildOpenPeriod(Purpose.PAA, PaaLevel.UNIT_LEVEL, today.minusDays(1)));
        }

        when(repository.findOpenExpired(eq(today), eq(BATCH_SIZE))).thenReturn(batch);
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenThrow(new RuntimeException("falha simulada de gravação"));

        scheduler.closeExpiredPeriods();

        verify(repository, times(1)).findOpenExpired(eq(today), eq(BATCH_SIZE));
    }

    @Test
    void scheduledCronDefaultsToHalfPastMidnightAndStaysOverridable() throws NoSuchMethodException {
        Method method = PaaSubmissionPeriodExpiryScheduler.class.getMethod("closeExpiredPeriods");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertEquals("${sigdi.paa.submission-period-expiry.cron:0 30 0 * * ?}", scheduled.cron());
    }
}
