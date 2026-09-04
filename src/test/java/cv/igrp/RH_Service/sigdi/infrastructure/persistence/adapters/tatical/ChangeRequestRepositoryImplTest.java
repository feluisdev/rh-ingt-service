package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingChangeRequestRow;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.ChangeRequestMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.ChangeRequestEntityRepository;

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

/**
 * Primeiro teste de {@link ChangeRequestRepositoryImpl}. O adaptador nunca teve teste próprio
 * antes desta fase (`grep -rl "ChangeRequestRepositoryImpl" src/test` devolvia vazio) --
 * só era exercido indiretamente pelos handlers de escrita.
 *
 * <p>{@code AuditEntity} tem {@code @Getter} e não tem {@code @Setter} -- não há forma de
 * construir uma {@code ChangeRequestEntity} real com {@code createdDate}/{@code createdBy}
 * preenchidos. Por isso as entidades aqui são {@code Mockito.mock(...)}, com os getters de
 * auditoria stubados diretamente.
 */
@ExtendWith(MockitoExtension.class)
class ChangeRequestRepositoryImplTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UUID ACTIVITY_ID = UUID.randomUUID();
    private static final String REQUESTED_BY = "6a1f2e3d-0000-4000-8000-000000000001";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 8, 1, 9, 0);

    @Mock
    private ChangeRequestEntityRepository jpaRepository;

    @Mock
    private ChangeRequestMapper mapper;

    @InjectMocks
    private ChangeRequestRepositoryImpl repository;

    private ChangeRequestEntity mockRequestEntity() {
        ChangeRequestEntity entity = org.mockito.Mockito.mock(ChangeRequestEntity.class);
        TacticalActivitiesEntity activity = org.mockito.Mockito.mock(TacticalActivitiesEntity.class);
        lenient().when(activity.getId()).thenReturn(ACTIVITY_ID);
        lenient().when(activity.getTitle()).thenReturn("Atividade-mãe");

        lenient().when(entity.getId()).thenReturn(REQUEST_ID);
        lenient().when(entity.getActivityId()).thenReturn(activity);
        lenient().when(entity.getFieldName()).thenReturn("budget");
        lenient().when(entity.getCurrentValue()).thenReturn("1000");
        lenient().when(entity.getProposedValue()).thenReturn("2500");
        lenient().when(entity.getCreatedBy()).thenReturn(REQUESTED_BY);
        lenient().when(entity.getCreatedDate()).thenReturn(REQUESTED_AT);
        return entity;
    }

    // Caso 1: os sete campos que a projeção acrescenta ao que a entidade já tinha -- incluindo
    // requestedBy/requestedAt, que vêm de getCreatedBy()/getCreatedDate() e não existem em
    // nenhum agregado de domínio (D-R). Prova negativa M2 mata este caso.
    @Test
    void findPendingRows_mapsAllSevenProjectionFieldsFromEntityGetters() {
        ChangeRequestEntity entity = mockRequestEntity();
        when(jpaRepository.findPendingWithActivity(eq(ChangeRequestStatus.PENDING.getCode()), any()))
                .thenReturn(new PageImpl<>(List.of(entity)));

        List<PendingChangeRequestRow> rows = repository.findPendingRows(0, 20);

        assertEquals(1, rows.size());
        PendingChangeRequestRow row = rows.get(0);
        assertEquals(REQUEST_ID, row.id());
        assertEquals("budget", row.fieldName());
        assertEquals("1000", row.currentValue());
        assertEquals("2500", row.proposedValue());
        assertEquals(REQUESTED_BY, row.requestedBy());
        assertEquals(REQUESTED_AT, row.requestedAt());
    }

    // Caso 2: activityTitle/activityId vêm de getActivityId().getTitle()/getId(), nunca de
    // entity.getId() (que é o id do PEDIDO, não da atividade) -- valores todos distintos entre
    // si para uma troca de campos ser detetável. Prova negativa M3 mata este caso.
    @Test
    void findPendingRows_readsActivityIdAndTitleFromTheJoinedActivityNotFromTheRequestItself() {
        ChangeRequestEntity entity = mockRequestEntity();
        when(jpaRepository.findPendingWithActivity(eq(ChangeRequestStatus.PENDING.getCode()), any()))
                .thenReturn(new PageImpl<>(List.of(entity)));

        PendingChangeRequestRow row = repository.findPendingRows(0, 20).get(0);

        assertEquals(ACTIVITY_ID, row.activityId());
        assertEquals("Atividade-mãe", row.activityTitle());
        assertNotEquals(row.id(), row.activityId());
    }

    // Caso 3: o Pageable passado leva Sort ascendente por createdDate (D-S) -- prova negativa
    // M1 (remover o Sort do PageRequest.of) mata este caso.
    @Test
    void findPendingRows_ordersOldestFirstByCreatedDateAndPassesThePageArgumentsThrough() {
        when(jpaRepository.findPendingWithActivity(any(), any())).thenReturn(Page.empty());

        repository.findPendingRows(2, 15);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findPendingWithActivity(eq(ChangeRequestStatus.PENDING.getCode()), captor.capture());
        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
        Sort.Order order = pageable.getSort().getOrderFor("createdDate");
        assertNotEquals(null, order);
        assertTrue(order.isAscending());
    }

    @Test
    void countPending_delegatesToCountByStatusWithThePendingCodeAndReturnsTheValueUnchanged() {
        when(jpaRepository.countByStatus(ChangeRequestStatus.PENDING.getCode())).thenReturn(7L);

        long count = repository.countPending();

        assertEquals(7L, count);
        verify(jpaRepository).countByStatus(ChangeRequestStatus.PENDING.getCode());
    }

    @Test
    void findPendingRows_withNoRowsReturnsAnEmptyListWithoutThrowing() {
        when(jpaRepository.findPendingWithActivity(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        List<PendingChangeRequestRow> rows = repository.findPendingRows(0, 20);

        assertTrue(rows.isEmpty());
    }
}
