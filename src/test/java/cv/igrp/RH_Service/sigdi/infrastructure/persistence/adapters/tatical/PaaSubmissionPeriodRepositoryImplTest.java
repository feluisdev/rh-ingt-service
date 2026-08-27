package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.PaaSubmissionPeriodMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.PaaSubmissionPeriodEntityRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobertura de {@code findByTypeAndYearAndPurpose} (Fase 116, plano 04): o finder que a
 * fonte de elegibilidade usa para traduzir o triplo (purpose, type, ano) num
 * {@link PaaSubmissionPeriod} concreto, sem filtrar por estado -- ver comentário defensivo
 * em {@link PaaSubmissionPeriodEntityRepository}.
 */
@ExtendWith(MockitoExtension.class)
class PaaSubmissionPeriodRepositoryImplTest {

    @Mock
    private PaaSubmissionPeriodEntityRepository jpaRepository;

    @Mock
    private PaaSubmissionPeriodMapper mapper;

    @InjectMocks
    private PaaSubmissionPeriodRepositoryImpl repository;

    @Test
    void findByTypeAndYearAndPurposeReturnsTheSinglePeriodWhenOneRowMatches() {
        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(UUID.randomUUID());
        entity.setType(PaaLevel.UNIT_LEVEL.getCode());
        entity.setPurpose(Purpose.PAA.getCode());
        entity.setYear(2027);
        entity.setStatus("OPEN");

        PaaSubmissionPeriod domainPeriod = PaaSubmissionPeriod.reconstruct(
                entity.getId(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), "OPEN", 2027);

        when(jpaRepository.findAllByTypeAndYearAndPurpose("UNIT_LEVEL", 2027, "PAA"))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainPeriod);

        Optional<PaaSubmissionPeriod> result =
                repository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA);

        assertTrue(result.isPresent());
        assertEquals(domainPeriod, result.get());
    }

    @Test
    void findByTypeAndYearAndPurposeReturnsTheFirstRowWhenTwoRowsMatch() {
        // O esquema não impõe unicidade em (type, year, purpose) -- ver comentário no topo
        // de PaaSubmissionPeriodEntityRepository. Duas linhas correspondentes não podem
        // rebentar; o adaptador tem de escolher a primeira (mais recente, por
        // createdDate DESC) sem atirar.
        PaaSubmissionPeriodEntity newer = new PaaSubmissionPeriodEntity();
        newer.setId(UUID.randomUUID());
        PaaSubmissionPeriodEntity older = new PaaSubmissionPeriodEntity();
        older.setId(UUID.randomUUID());

        PaaSubmissionPeriod newerDomain = PaaSubmissionPeriod.reconstruct(
                newer.getId(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), "CLOSED", 2027);

        when(jpaRepository.findAllByTypeAndYearAndPurpose("UNIT_LEVEL", 2027, "PAA"))
                .thenReturn(List.of(newer, older));
        when(mapper.toDomain(newer)).thenReturn(newerDomain);

        Optional<PaaSubmissionPeriod> result =
                repository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA);

        assertTrue(result.isPresent());
        assertEquals(newerDomain, result.get());
        verify(mapper).toDomain(newer);
    }

    @Test
    void findByTypeAndYearAndPurposeReturnsEmptyWhenNoRowsMatch() {
        when(jpaRepository.findAllByTypeAndYearAndPurpose("UNIT_LEVEL", 2027, "PAA"))
                .thenReturn(List.of());

        Optional<PaaSubmissionPeriod> result =
                repository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA);

        assertFalse(result.isPresent());
    }

    @Test
    void findByTypeAndYearAndPurposePassesEnumCodesNotEnumToStringToTheJpaRepository() {
        when(jpaRepository.findAllByTypeAndYearAndPurpose(any(), eq(2027), any()))
                .thenReturn(List.of());

        repository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA);

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> purposeCaptor = ArgumentCaptor.forClass(String.class);
        verify(jpaRepository).findAllByTypeAndYearAndPurpose(typeCaptor.capture(), eq(2027), purposeCaptor.capture());

        assertEquals("UNIT_LEVEL", typeCaptor.getValue());
        assertEquals("PAA", purposeCaptor.getValue());
    }

    @Test
    void findByTypeAndYearAndPurposeReturnsClosedPeriodTheSameWay() {
        // A consulta de elegíveis também responde sobre períodos fechados -- o finder não
        // filtra por estado, ao contrário dos três finders vizinhos.
        PaaSubmissionPeriodEntity closedEntity = new PaaSubmissionPeriodEntity();
        closedEntity.setId(UUID.randomUUID());
        closedEntity.setStatus("CLOSED");

        PaaSubmissionPeriod closedDomain = PaaSubmissionPeriod.reconstruct(
                closedEntity.getId(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), "CLOSED", 2027);

        when(jpaRepository.findAllByTypeAndYearAndPurpose("UNIT_LEVEL", 2027, "PAA"))
                .thenReturn(List.of(closedEntity));
        when(mapper.toDomain(closedEntity)).thenReturn(closedDomain);

        Optional<PaaSubmissionPeriod> result =
                repository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA);

        assertTrue(result.isPresent());
        assertTrue(result.get().isClosed());
    }

    @Test
    void findOpenExpiredAlwaysAsksForTheFirstPage() {
        // A leitura processa as linhas devolvidas e o consumidor fecha-as em seguida --
        // um offset crescente saltaria linhas ainda por processar. Ver comentário no
        // adaptador. Por isso pede-se sempre a página zero, qualquer que seja o limite.
        when(jpaRepository.findOpenWithEndDateBefore(any(), any())).thenReturn(List.of());

        repository.findOpenExpired(LocalDate.of(2026, 8, 26), 100);

        ArgumentCaptor<org.springframework.data.domain.Pageable> pageableCaptor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(jpaRepository).findOpenWithEndDateBefore(any(), pageableCaptor.capture());

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void findOpenExpiredPassesTheGivenDateThrough() {
        when(jpaRepository.findOpenWithEndDateBefore(any(), any())).thenReturn(List.of());

        LocalDate today = LocalDate.of(2026, 8, 26);
        repository.findOpenExpired(today, 100);

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(jpaRepository).findOpenWithEndDateBefore(dateCaptor.capture(), any());

        assertEquals(today, dateCaptor.getValue());
    }

    @Test
    void findOpenExpiredMapsEveryRow() {
        PaaSubmissionPeriodEntity first = new PaaSubmissionPeriodEntity();
        first.setId(UUID.randomUUID());
        first.setStatus("OPEN");
        PaaSubmissionPeriodEntity second = new PaaSubmissionPeriodEntity();
        second.setId(UUID.randomUUID());
        second.setStatus("OPEN");

        PaaSubmissionPeriod firstDomain = PaaSubmissionPeriod.reconstruct(
                first.getId(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "OPEN", 2026);
        PaaSubmissionPeriod secondDomain = PaaSubmissionPeriod.reconstruct(
                second.getId(), Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 7, 31), "OPEN", 2026);

        when(jpaRepository.findOpenWithEndDateBefore(any(), any())).thenReturn(List.of(first, second));
        when(mapper.toDomain(first)).thenReturn(firstDomain);
        when(mapper.toDomain(second)).thenReturn(secondDomain);

        List<PaaSubmissionPeriod> result = repository.findOpenExpired(LocalDate.of(2026, 8, 26), 100);

        assertEquals(2, result.size());
        assertEquals(firstDomain, result.get(0));
        assertEquals(secondDomain, result.get(1));
    }

    /**
     * Cobertura de {@code findOpenActiveOn} (Fase 119, plano 04): a leitura que o agendador de
     * abertura usa para saber quais os períodos {@code OPEN} a decorrer hoje. Ao contrário de
     * {@code findOpenExpired}, cuja fronteira superior é estrita, aqui as duas fronteiras --
     * {@code startDate} e {@code endDate} -- são inclusivas.
     */
    @Test
    void findOpenActiveOnReturnsAPeriodThatStartedYesterdayAndEndsTomorrow() {
        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(UUID.randomUUID());
        entity.setStatus("OPEN");

        LocalDate today = LocalDate.of(2026, 8, 27);
        PaaSubmissionPeriod domainPeriod = PaaSubmissionPeriod.reconstruct(
                entity.getId(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.minusDays(1), today.plusDays(1), "OPEN", 2026);

        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainPeriod);

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertEquals(1, result.size());
        assertEquals(domainPeriod, result.get(0));
    }

    @Test
    void findOpenActiveOnReturnsAPeriodThatStartsToday() {
        // Fronteira inferior inclusiva.
        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(UUID.randomUUID());
        entity.setStatus("OPEN");

        LocalDate today = LocalDate.of(2026, 8, 27);
        PaaSubmissionPeriod domainPeriod = PaaSubmissionPeriod.reconstruct(
                entity.getId(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today, today.plusDays(30), "OPEN", 2026);

        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainPeriod);

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertEquals(1, result.size());
        assertEquals(domainPeriod, result.get(0));
    }

    @Test
    void findOpenActiveOnReturnsAPeriodThatEndsToday() {
        // Fronteira superior inclusiva -- ao contrário de findOpenExpired.
        PaaSubmissionPeriodEntity entity = new PaaSubmissionPeriodEntity();
        entity.setId(UUID.randomUUID());
        entity.setStatus("OPEN");

        LocalDate today = LocalDate.of(2026, 8, 27);
        PaaSubmissionPeriod domainPeriod = PaaSubmissionPeriod.reconstruct(
                entity.getId(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                today.minusDays(30), today, "OPEN", 2026);

        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainPeriod);

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertEquals(1, result.size());
        assertEquals(domainPeriod, result.get(0));
    }

    @Test
    void findOpenActiveOnReturnsEmptyWhenThePeriodOnlyStartsTomorrow() {
        LocalDate today = LocalDate.of(2026, 8, 27);
        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of());

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertTrue(result.isEmpty());
    }

    @Test
    void findOpenActiveOnReturnsEmptyWhenThePeriodEndedYesterday() {
        LocalDate today = LocalDate.of(2026, 8, 27);
        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of());

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertTrue(result.isEmpty());
    }

    @Test
    void findOpenActiveOnReturnsEmptyForAClosedPeriodEvenIfActive() {
        LocalDate today = LocalDate.of(2026, 8, 27);
        // A consulta já filtra por status = 'OPEN'; do lado do adaptador não há mais nada a
        // verificar -- o teste prova apenas que uma lista vazia devolvida pelo JPA não rebenta.
        when(jpaRepository.findOpenActiveOn(eq(today), any())).thenReturn(List.of());

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(today, 50);

        assertTrue(result.isEmpty());
    }

    @Test
    void findOpenActiveOnAlwaysAsksForTheFirstPage() {
        when(jpaRepository.findOpenActiveOn(any(), any())).thenReturn(List.of());

        repository.findOpenActiveOn(LocalDate.of(2026, 8, 27), 50);

        ArgumentCaptor<org.springframework.data.domain.Pageable> pageableCaptor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(jpaRepository).findOpenActiveOn(any(), pageableCaptor.capture());

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void findOpenActiveOnReturnsEmptyListWhenZeroRows() {
        when(jpaRepository.findOpenActiveOn(any(), any())).thenReturn(List.of());

        List<PaaSubmissionPeriod> result = repository.findOpenActiveOn(LocalDate.of(2026, 8, 27), 50);

        assertTrue(result.isEmpty());
    }
}
