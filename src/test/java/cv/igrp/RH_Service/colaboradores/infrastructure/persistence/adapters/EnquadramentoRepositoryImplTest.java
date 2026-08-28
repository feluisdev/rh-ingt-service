package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.EnquadramentoEntityRepository;

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
 * Prova de {@link EnquadramentoRepositoryImpl#findAllByUnidadeOrganicaIdCoveringYear}.
 *
 * <p>Este teste prova duas coisas: (a) que o ano de entrada é traduzido correctamente
 * nas duas datas-limite passadas ao {@code @Query} JPQL -- {@code LocalDate.of(year, 1, 1)}
 * e {@code LocalDate.of(year, 12, 31)}, capturadas via {@link ArgumentCaptor}; (b) que o
 * resultado passa pelo {@link EnquadramentoMapper} e sai como
 * {@code List<EnquadramentoProfissional>} na mesma ordem devolvida pelo JPA.
 *
 * <p>Os sete casos de inclusão/exclusão por datas descritos no plano (enquadramento que
 * começa depois do ano, termina antes do ano, cobre um único dia no limite inferior ou
 * superior, unidade errada, {@code isCurrent = false} que ainda assim é devolvido)
 * pertencem ao predicado JPQL em si -- exercem-se apenas contra uma base de dados real.
 * Este módulo não tem infra-estrutura de teste com base de dados; este plano não a
 * inventa. Aqui fixam-se as datas-limite calculadas, que é a parte do comportamento que
 * o adaptador (e não o JPQL) é responsável por produzir.
 */
@ExtendWith(MockitoExtension.class)
class EnquadramentoRepositoryImplTest {

    private static final UUID UNIDADE_ORGANICA_ID = UUID.randomUUID();

    @Mock
    private EnquadramentoEntityRepository entityRepository;

    @Mock
    private EnquadramentoMapper mapper;

    @InjectMocks
    private EnquadramentoRepositoryImpl repository;

    @Test
    void findAllByUnidadeOrganicaIdCoveringYear_translatesYearIntoFirstAndLastDayBoundaries() {
        int year = 2027;
        when(entityRepository.findAllByUnidadeOrganicaIdCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        repository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, year);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(entityRepository).findAllByUnidadeOrganicaIdCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                startCaptor.capture(),
                endCaptor.capture());

        assertEquals(LocalDate.of(year, 1, 1), startCaptor.getValue());
        assertEquals(LocalDate.of(year, 12, 31), endCaptor.getValue());
    }

    @Test
    void findAllByUnidadeOrganicaIdCoveringYear_mapsResultThroughMapperPreservingOrder() {
        int year = 2027;
        EnquadramentoEntity firstEntity = mockEntity();
        EnquadramentoEntity secondEntity = mockEntity();
        EnquadramentoProfissional firstDomain = mockDomain();
        EnquadramentoProfissional secondDomain = mockDomain();

        when(entityRepository.findAllByUnidadeOrganicaIdCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(firstEntity, secondEntity));
        when(mapper.toDomain(firstEntity)).thenReturn(firstDomain);
        when(mapper.toDomain(secondEntity)).thenReturn(secondDomain);

        List<EnquadramentoProfissional> result =
                repository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, year);

        assertEquals(2, result.size());
        assertEquals(firstDomain, result.get(0));
        assertEquals(secondDomain, result.get(1));
    }

    @Test
    void findAllByUnidadeOrganicaIdCoveringYear_withNoMatchesReturnsEmptyList() {
        when(entityRepository.findAllByUnidadeOrganicaIdCoveringRange(
                org.mockito.ArgumentMatchers.eq(UNIDADE_ORGANICA_ID),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<EnquadramentoProfissional> result =
                repository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, 2027);

        assertTrue(result.isEmpty());
    }

    private EnquadramentoEntity mockEntity() {
        return org.mockito.Mockito.mock(EnquadramentoEntity.class);
    }

    private EnquadramentoProfissional mockDomain() {
        return EnquadramentoProfissional.reconstituir(
                EnquadramentoId.from(UUID.randomUUID()),
                FuncionarioId.from(UUID.randomUUID()),
                null, null, null,
                UUID.randomUUID(),
                null,
                UNIDADE_ORGANICA_ID,
                LocalDate.of(2027, 1, 1),
                null,
                Boolean.FALSE
        );
    }
}
