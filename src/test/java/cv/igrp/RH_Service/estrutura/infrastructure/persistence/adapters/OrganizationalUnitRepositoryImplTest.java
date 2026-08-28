package cv.igrp.RH_Service.estrutura.infrastructure.persistence.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.OrganizationalUnitEntityRepository;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link OrganizationalUnitRepositoryImpl#findAllActive}.
 *
 * <p>O caso que interessa é o que fixa a decisão "só unidades activas, sem paginação"
 * contra uma alteração futura distraída: {@code findByIsActiveTrue()} é chamado e
 * {@code findAll(Specification, Pageable)} nunca é -- reutilizar o {@code findAll}
 * paginado já existente devolveria só os primeiros 20 registos por omissão, o que
 * partiria silenciosamente a fonte de elegíveis.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationalUnitRepositoryImplTest {

    @Mock
    private OrganizationalUnitEntityRepository entityRepository;

    @Mock
    private OrganizationalUnitMapper mapper;

    @InjectMocks
    private OrganizationalUnitRepositoryImpl repository;

    @Test
    void findAllActive_delegatesToFindByIsActiveTrueAndNeverCallsPaginatedFindAll() {
        when(entityRepository.findByIsActiveTrue()).thenReturn(List.of());

        repository.findAllActive();

        verify(entityRepository).findByIsActiveTrue();
        verify(entityRepository, never()).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void findAllActive_mapsResultThroughMapperPreservingOrder() {
        OrganizationalUnitEntity firstEntity = org.mockito.Mockito.mock(OrganizationalUnitEntity.class);
        OrganizationalUnitEntity secondEntity = org.mockito.Mockito.mock(OrganizationalUnitEntity.class);
        OrganizationalUnit firstDomain = mockDomain();
        OrganizationalUnit secondDomain = mockDomain();

        when(entityRepository.findByIsActiveTrue()).thenReturn(List.of(firstEntity, secondEntity));
        when(mapper.toDomain(firstEntity)).thenReturn(firstDomain);
        when(mapper.toDomain(secondEntity)).thenReturn(secondDomain);

        List<OrganizationalUnit> result = repository.findAllActive();

        assertEquals(2, result.size());
        assertEquals(firstDomain, result.get(0));
        assertEquals(secondDomain, result.get(1));
    }

    @Test
    void findAllActive_withNoActiveUnitsReturnsEmptyListNeverNull() {
        when(entityRepository.findByIsActiveTrue()).thenReturn(List.of());

        List<OrganizationalUnit> result = repository.findAllActive();

        assertTrue(result.isEmpty());
    }

    private OrganizationalUnit mockDomain() {
        return OrganizationalUnit.reconstruir(
                OrganizationalUnitId.from(UUID.randomUUID()),
                "COD",
                "Nome",
                "ACR",
                "DIRECAO",
                null,
                null,
                UUID.randomUUID(),
                true
        );
    }
}
