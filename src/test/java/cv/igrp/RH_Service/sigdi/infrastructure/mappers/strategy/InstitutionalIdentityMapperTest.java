package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link InstitutionalIdentityMapper}, focused on the {@code createdAt}
 * field added in phase 79 (DATA-03 backend half): the identity "Historico de Versoes"
 * panel always rendered a null/epoch date because InstitutionalIdentity carried no date
 * field at all. Covers both directions named by the plan: a reconstructed (already
 * persisted) identity must expose its real createdAt on the response, while a brand-new
 * (create()) identity must never fabricate one.
 */
class InstitutionalIdentityMapperTest {

    private final InstitutionalIdentityMapper mapper =
            new InstitutionalIdentityMapper(new StrategicGoalMapper(null));

    @Test
    void toResponse_reconstructedIdentity_exposesPopulatedCreatedAt() {
        LocalDateTime createdDate = LocalDateTime.of(2026, 7, 16, 16, 22, 31);

        InstitutionalIdentity domain = InstitutionalIdentity.reconstruct(
                InstitutionalIdentityId.gerarNovo(),
                UUID.randomUUID(),
                2026,
                "Missao de teste",
                "Visao de teste",
                InstitutionalValues.of(List.of("Integridade", "Transparencia")),
                "Versao inicial",
                true,
                List.of(),
                createdDate);

        assertEquals(createdDate, domain.getCreatedAt());

        IdentityResponseDTO response = mapper.toResponse(domain);

        assertEquals(createdDate, response.getCreatedAt());
    }

    @Test
    void toResponse_newlyCreatedIdentity_createdAtStaysNull() {
        InstitutionalIdentity domain = InstitutionalIdentity.create(
                UUID.randomUUID(),
                2026,
                "Missao de teste",
                "Visao de teste",
                InstitutionalValues.of(List.of("Integridade", "Transparencia")),
                "Versao inicial");

        assertNull(domain.getCreatedAt());

        IdentityResponseDTO response = mapper.toResponse(domain);

        assertNull(response.getCreatedAt());
    }
}
