package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link OrganicaLookupAdapter#findAllActiveUnits()}.
 *
 * <p>Cobre os cinco casos do bloco {@code behavior} do {@code 116-02-PLAN.md}: três
 * unidades activas mapeadas para {@link OrganicaDTO}, cada DTO com id/name/acronym/
 * responsibleEmployeeId em texto, uma unidade sem responsável a sair com
 * {@code responsibleEmployeeId == null} (nunca a string {@code "null"}), repositório vazio
 * devolve lista vazia nunca {@code null}, e a ordem do repositório é preservada.
 */
@ExtendWith(MockitoExtension.class)
class OrganicaLookupAdapterTest {

    @Mock
    private OrganizationalUnitRepository repository;

    @InjectMocks
    private OrganicaLookupAdapter adapter;

    private static OrganizationalUnit unidade(String name, String acronym, UUID responsibleEmployeeId) {
        return OrganizationalUnit.reconstruir(
                OrganizationalUnitId.gerarNovo(),
                "COD-" + name,
                name,
                acronym,
                "DIRECAO",
                "descricao",
                null,
                responsibleEmployeeId,
                true);
    }

    @Test
    void findAllActiveUnits_comTresUnidadesActivas_devolveTresDtos() {
        OrganizationalUnit u1 = unidade("Unidade 1", "U1", UUID.randomUUID());
        OrganizationalUnit u2 = unidade("Unidade 2", "U2", UUID.randomUUID());
        OrganizationalUnit u3 = unidade("Unidade 3", "U3", UUID.randomUUID());
        when(repository.findAllActive()).thenReturn(List.of(u1, u2, u3));

        List<OrganicaDTO> resultado = adapter.findAllActiveUnits();

        assertEquals(3, resultado.size());
    }

    @Test
    void findAllActiveUnits_cadaDtoTrazIdNameAcronymEResponsibleEmployeeIdEmTexto() {
        UUID responsavel = UUID.randomUUID();
        OrganizationalUnit unidade = unidade("Unidade 1", "U1", responsavel);
        when(repository.findAllActive()).thenReturn(List.of(unidade));

        OrganicaDTO dto = adapter.findAllActiveUnits().get(0);

        assertEquals(unidade.getId().getValor().toString(), dto.getId());
        assertEquals("Unidade 1", dto.getName());
        assertEquals("U1", dto.getAcronym());
        assertEquals(responsavel.toString(), dto.getResponsibleEmployeeId());
    }

    @Test
    void findAllActiveUnits_comResponsibleEmployeeIdNulo_dtoSaiComNullENaoComAStringNull() {
        OrganizationalUnit unidade = unidade("Unidade Sem Responsavel", "USR", null);
        when(repository.findAllActive()).thenReturn(List.of(unidade));

        OrganicaDTO dto = adapter.findAllActiveUnits().get(0);

        assertNull(dto.getResponsibleEmployeeId());
    }

    @Test
    void findAllActiveUnits_comRepositorioVazio_devolveListaVaziaNuncaNull() {
        when(repository.findAllActive()).thenReturn(List.of());

        List<OrganicaDTO> resultado = adapter.findAllActiveUnits();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findAllActiveUnits_preservaAOrdemDoRepositorio() {
        OrganizationalUnit u1 = unidade("Unidade A", "UA", UUID.randomUUID());
        OrganizationalUnit u2 = unidade("Unidade B", "UB", UUID.randomUUID());
        when(repository.findAllActive()).thenReturn(List.of(u1, u2));

        List<OrganicaDTO> resultado = adapter.findAllActiveUnits();

        assertEquals(u1.getId().getValor().toString(), resultado.get(0).getId());
        assertEquals(u2.getId().getValor().toString(), resultado.get(1).getId());
    }
}
