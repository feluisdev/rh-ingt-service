package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingActivityRow;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.TacticalActivityMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Primeiro teste de {@link TacticalActivityRepositoryImpl#findPendingRows}. O restante do
 * adaptador (`findByStatuses`/`countByStatuses`) não é tocado por este plano.
 *
 * <p>{@code AuditEntity} não tem {@code @Setter} -- as entidades usadas aqui são
 * {@code Mockito.mock(...)}, com os getters de auditoria stubados diretamente.
 */
@ExtendWith(MockitoExtension.class)
class TacticalActivityRepositoryImplTest {

    private static final UUID ACTIVITY_ID = UUID.randomUUID();
    private static final String REQUESTED_BY = "6a1f2e3d-0000-4000-8000-000000000002";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 8, 2, 10, 0);

    @Mock
    private TacticalActivitiesEntityRepository jpaRepository;

    @Mock
    private TacticalActivityMapper mapper;

    @InjectMocks
    private TacticalActivityRepositoryImpl repository;

    private TacticalActivitiesEntity mockActivityEntity() {
        TacticalActivitiesEntity entity = org.mockito.Mockito.mock(TacticalActivitiesEntity.class);
        lenient().when(entity.getId()).thenReturn(ACTIVITY_ID);
        lenient().when(entity.getTitle()).thenReturn("Reforçar a rede de saneamento");
        lenient().when(entity.getStatus()).thenReturn("PENDING_TACTICAL");
        lenient().when(entity.getBudgetEstimated()).thenReturn(new BigDecimal("40000"));
        lenient().when(entity.getEconomicClassifier()).thenReturn("02.02.01");
        lenient().when(entity.getCreatedBy()).thenReturn(REQUESTED_BY);
        lenient().when(entity.getCreatedDate()).thenReturn(REQUESTED_AT);
        return entity;
    }

    // Correspondência dos sete componentes de PendingActivityRow com os getters stubados,
    // incluindo requestedBy/requestedAt vindos de getCreatedBy()/getCreatedDate(). Prova
    // negativa M2 (adaptada a este ficheiro) mata este caso.
    @Test
    void findPendingRows_mapsAllSevenProjectionFieldsFromEntityGetters() {
        TacticalActivitiesEntity entity = mockActivityEntity();
        when(jpaRepository.findAll(org.mockito.ArgumentMatchers.<Specification<TacticalActivitiesEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        List<PendingActivityRow> rows = repository.findPendingRows(List.of("PENDING_TACTICAL", "PENDING_STRATEGIC"), 0, 20);

        assertEquals(1, rows.size());
        PendingActivityRow row = rows.get(0);
        assertEquals(ACTIVITY_ID, row.id());
        assertEquals("Reforçar a rede de saneamento", row.title());
        assertEquals("PENDING_TACTICAL", row.status());
        assertEquals(0, new BigDecimal("40000").compareTo(row.budgetEstimated()));
        assertEquals("02.02.01", row.economicClassifier());
        assertEquals(REQUESTED_BY, row.requestedBy());
        assertEquals(REQUESTED_AT, row.requestedAt());
    }

    // O Pageable leva Sort ascendente por createdDate (D-S), e a Specification passada não é
    // nula. Prova negativa M1 mata a parte da ordenação deste caso.
    @Test
    void findPendingRows_ordersOldestFirstByCreatedDateAndPassesANonNullSpecification() {
        when(jpaRepository.findAll(org.mockito.ArgumentMatchers.<Specification<TacticalActivitiesEntity>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        repository.findPendingRows(List.of("PENDING_TACTICAL"), 1, 10);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<TacticalActivitiesEntity>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

        assertNotNull(specCaptor.getValue());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("createdDate");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void findPendingRows_withNoRowsReturnsAnEmptyListWithoutThrowing() {
        when(jpaRepository.findAll(org.mockito.ArgumentMatchers.<Specification<TacticalActivitiesEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        List<PendingActivityRow> rows = repository.findPendingRows(List.of("PENDING_TACTICAL"), 0, 20);

        assertTrue(rows.isEmpty());
    }
}
