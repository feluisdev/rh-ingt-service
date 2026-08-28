package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobertura de {@link EligibleResponsiblesSource} (AUT-06, Fase 116, plano 04): a porta
 * que traduz o triplo (purpose, type, ano) num {@link PaaSubmissionPeriod} e delega no
 * {@link EligibleResponsiblesResolver}. Molde de asserção de erro copiado de
 * {@code GetActiveSubmissionPeriodQueryHandlerTest}.
 */
@ExtendWith(MockitoExtension.class)
class EligibleResponsiblesSourceTest {

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @Mock
    private EligibleResponsiblesResolver resolver;

    @InjectMocks
    private EligibleResponsiblesSource source;

    @Test
    void findByReturnsTheResolverDtoUnchangedWhenPeriodExists() {
        PaaSubmissionPeriod period = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), "OPEN", 2027);
        EligibleResponsiblesDTO expected = new EligibleResponsiblesDTO();

        when(periodRepository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA))
                .thenReturn(Optional.of(period));
        when(resolver.resolve(period)).thenReturn(expected);

        EligibleResponsiblesDTO result = source.findBy(Purpose.PAA, PaaLevel.UNIT_LEVEL, 2027);

        assertSame(expected, result);
    }

    @Test
    void findByReturnsTheResolverDtoUnchangedWhenPeriodIsClosed() {
        // O estado do período não é filtro da consulta -- responde na mesma.
        PaaSubmissionPeriod closedPeriod = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), "CLOSED", 2027);
        EligibleResponsiblesDTO expected = new EligibleResponsiblesDTO();

        when(periodRepository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA))
                .thenReturn(Optional.of(closedPeriod));
        when(resolver.resolve(closedPeriod)).thenReturn(expected);

        EligibleResponsiblesDTO result = source.findBy(Purpose.PAA, PaaLevel.UNIT_LEVEL, 2027);

        assertSame(expected, result);
    }

    @Test
    void findByThrowsNotFoundWithReadableNamesWhenNoPeriodMatchesTheTriple() {
        when(periodRepository.findByTypeAndYearAndPurpose(PaaLevel.UNIT_LEVEL, 2027, Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> source.findBy(Purpose.PAA, PaaLevel.UNIT_LEVEL, 2027));

        assertEquals(404, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(Purpose.PAA.getDescription()));
        assertTrue(exception.getBody().getTitle().contains(PaaLevel.UNIT_LEVEL.getDescription()));
        assertTrue(exception.getBody().getTitle().contains("2027"));
        verify(resolver, never()).resolve(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findByThrowsBadRequestWhenYearIsNull() {
        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> source.findBy(Purpose.PAA, PaaLevel.UNIT_LEVEL, null));

        assertEquals(400, exception.getBody().getStatus());
        verifyNoInteractions(periodRepository, resolver);
    }

    @Test
    void findByThrowsBadRequestWhenPurposeIsNull() {
        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> source.findBy(null, PaaLevel.UNIT_LEVEL, 2027));

        assertEquals(400, exception.getBody().getStatus());
        verifyNoInteractions(periodRepository, resolver);
    }

    @Test
    void findByThrowsBadRequestWhenTypeIsNull() {
        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> source.findBy(Purpose.PAA, null, 2027));

        assertEquals(400, exception.getBody().getStatus());
        verifyNoInteractions(periodRepository, resolver);
    }
}
