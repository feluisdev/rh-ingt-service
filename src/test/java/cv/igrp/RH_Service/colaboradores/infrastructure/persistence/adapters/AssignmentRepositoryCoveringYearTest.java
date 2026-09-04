package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.AssignmentMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsAssignmentEntityRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link AssignmentRepositoryImpl#findAllByUnidadeOrganicaCoveringYear}.
 *
 * <p>Este teste prova duas coisas: (a) que o ano de entrada é traduzido correctamente
 * nas duas datas-limite passadas ao {@code @Query} JPQL -- {@code LocalDate.of(year, 1, 1)}
 * e {@code LocalDate.of(year, 12, 31)}, capturadas via {@link ArgumentCaptor}; (b) que o
 * resultado passa pelo {@link AssignmentMapper} e sai como {@code List<Assignment>} na
 * mesma ordem devolvida pelo JPA.
 *
 * <p>Os casos de inclusão/exclusão por datas (afectação que começa depois do ano, termina
 * antes do ano, cobre um único dia num dos limites, unidade errada, {@code isCurrent = false}
 * que ainda assim é devolvido) pertencem ao predicado JPQL em si -- e agora também ao join
 * sobre {@code PositionEntity}, já que a unidade orgânica vive no Lugar. Exercem-se apenas
 * contra uma base de dados real; este módulo não tem infra-estrutura de teste com base de
 * dados. Aqui fixam-se as datas-limite calculadas, que é a parte do comportamento que o
 * adaptador (e não o JPQL) é responsável por produzir.
 *
 * <p>Portado de {@code EnquadramentoRepositoryImplTest} (feat/116) após a eliminação do
 * modelo de enquadramento no Bloco B: o comportamento provado é o mesmo, a fonte da
 * unidade orgânica mudou de enquadramento para Lugar.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentRepositoryCoveringYearTest {

    private static final UUID UNIDADE_ORGANICA_ID = UUID.randomUUID();

    @Mock
    private ColabsAssignmentEntityRepository entityRepository;

    @Mock
    private AssignmentMapper mapper;

    @InjectMocks
    private AssignmentRepositoryImpl repository;

    @Test
    void findAllByUnidadeOrganicaCoveringYear_translatesYearIntoFirstAndLastDayBoundaries() {
        int year = 2027;
        when(entityRepository.findAllByUnidadeOrganicaCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        repository.findAllByUnidadeOrganicaCoveringYear(UNIDADE_ORGANICA_ID, year);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(entityRepository).findAllByUnidadeOrganicaCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                startCaptor.capture(),
                endCaptor.capture());

        assertEquals(LocalDate.of(year, 1, 1), startCaptor.getValue());
        assertEquals(LocalDate.of(year, 12, 31), endCaptor.getValue());
    }

    @Test
    void findAllByUnidadeOrganicaCoveringYear_mapsResultThroughMapperPreservingOrder() {
        int year = 2027;
        AssignmentEntity firstEntity = org.mockito.Mockito.mock(AssignmentEntity.class);
        AssignmentEntity secondEntity = org.mockito.Mockito.mock(AssignmentEntity.class);
        Assignment firstDomain = mockDomain();
        Assignment secondDomain = mockDomain();

        when(entityRepository.findAllByUnidadeOrganicaCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(firstEntity, secondEntity));
        when(mapper.toDomain(firstEntity)).thenReturn(firstDomain);
        when(mapper.toDomain(secondEntity)).thenReturn(secondDomain);

        List<Assignment> result = repository.findAllByUnidadeOrganicaCoveringYear(UNIDADE_ORGANICA_ID, year);

        assertEquals(2, result.size());
        assertEquals(firstDomain, result.get(0));
        assertEquals(secondDomain, result.get(1));
    }

    @Test
    void findAllByUnidadeOrganicaCoveringYear_withNoMatchesReturnsEmptyList() {
        when(entityRepository.findAllByUnidadeOrganicaCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<Assignment> result = repository.findAllByUnidadeOrganicaCoveringYear(UNIDADE_ORGANICA_ID, 2027);

        assertTrue(result.isEmpty());
    }

    private Assignment mockDomain() {
        return Assignment.reconstituir(
                AssignmentId.from(UUID.randomUUID()),
                FuncionarioId.from(UUID.randomUUID()),
                UUID.randomUUID(),
                null, null,
                Assignment.PRINCIPAL,
                "NOMEACAO",
                null,
                LocalDate.of(2027, 1, 1),
                null,
                true,
                true,
                null);
    }
}
