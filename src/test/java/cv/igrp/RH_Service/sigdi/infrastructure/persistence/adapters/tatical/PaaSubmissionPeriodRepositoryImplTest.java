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
}
