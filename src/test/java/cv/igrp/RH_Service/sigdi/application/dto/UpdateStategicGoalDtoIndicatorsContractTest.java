package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Deserialization contract for the {@code indicators} field of {@link UpdateStategicGoalDTO}.
 *
 * <p>Why this test exists, and why it does not live inside the DTO. The DTO file carries the
 * header {@code GENERATED AUTOMATICALLY BY iGRP STUDIO -- DO NOT MODIFY}. A regeneration would
 * restore the empty-list initializer on the {@code indicators} field, and from that moment on an
 * ABSENT {@code "indicators"} key would once again be indistinguishable from an EMPTY list:
 * Jackson would hand the handler a non-null empty list either way, and editing a strategic goal
 * would once again delete every KPI it has. That is finding {@code A-124-01}, reproduced and
 * measured.
 *
 * <p>{@link cv.igrp.RH_Service.sigdi.application.commands.UpdateStrategicGoalsCommandHandler}
 * holds the guard that decides between "do not touch" and "replace", but that guard cannot defend
 * itself: the information it needs is destroyed upstream, in this DTO, before the handler runs.
 * <b>This test is the half of the fix that survives a regeneration</b> -- it turns the regression
 * from silent data loss into a red build. It is placed outside the generated file on purpose, so
 * that regenerating the DTO cannot delete the guard along with the fix.
 *
 * <p>Precedent in this repository: {@code LobColumnSqlGuardTest} exists for exactly this kind of
 * job -- keeping a closed incident from quietly reopening.
 */
class UpdateStategicGoalDtoIndicatorsContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("An absent indicators key deserializes to null -- absent means 'do not touch'")
    void absentIndicatorsKeyDeserializesToNull() throws Exception {
        String body = "{\"title\":\"Objetivo sem a chave de indicadores\","
                + "\"description\":\"Descrição\",\"weight\":1.00,\"year\":2026}";

        UpdateStategicGoalDTO dto = objectMapper.readValue(body, UpdateStategicGoalDTO.class);

        assertNull(dto.getIndicators(),
                "The indicators field must stay null when the key is absent. If this fails, the DTO "
                        + "field was initialized again -- most likely by an iGRP Studio regeneration -- "
                        + "and UpdateStrategicGoalsCommandHandler can no longer tell an absent list from "
                        + "an empty one, which reopens finding A-124-01: editing a goal wipes its KPIs.");
    }

    @Test
    @DisplayName("An explicit empty indicators list deserializes to a non-null empty list -- [] means 'remove all'")
    void explicitEmptyIndicatorsListDeserializesToEmptyList() throws Exception {
        String body = "{\"title\":\"Objetivo com lista vazia explícita\","
                + "\"description\":\"Descrição\",\"weight\":1.00,\"year\":2026,\"indicators\":[]}";

        UpdateStategicGoalDTO dto = objectMapper.readValue(body, UpdateStategicGoalDTO.class);

        assertNotNull(dto.getIndicators(),
                "An explicit [] must arrive as a non-null list, otherwise removing the last KPI "
                        + "becomes impossible: there is no dedicated indicator endpoint, so this is the "
                        + "only route the product offers.");
        assertTrue(dto.getIndicators().isEmpty());
        assertEquals(0, dto.getIndicators().size());
    }
}
