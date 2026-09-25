package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Serialization contract for the two fields wave 4 of Phase 130 added to iGRP-Studio-generated
 * DTOs: {@code perspective} on {@link UpdateStategicGoalDTO} and {@code incoherentLinks} on
 * {@link StategicGoalResponseDTO}.
 *
 * <p><b>Why this test exists and why it does not live inside either DTO.</b> Both files carry the
 * header {@code GENERATED AUTOMATICALLY BY iGRP STUDIO -- DO NOT MODIFY}. A regeneration would
 * delete both fields outright. The consequences are not symmetrical, and neither is loud:
 *
 * <ul>
 *   <li>Losing {@code perspective} on the request DTO reopens finding {@code A-124-02} exactly as
 *       it was measured: the client sends the field, Jackson has nowhere to put it, the service
 *       does not configure {@code fail-on-unknown-properties}, and the value is dropped while the
 *       response says 200 and the product says "As alterações foram gravadas com sucesso."
 *   <li>Losing {@code incoherentLinks} on the response DTO does not break a single request. It
 *       silently stops the warning of decision 2 of {@code 130-CONTEXT.md} from reaching anyone,
 *       which is the "save in silence" branch that finding {@code A-126-05} already classified.
 * </ul>
 *
 * <p><b>This test is the half of the fix that survives a regeneration</b> -- it turns either
 * regression from silent data loss into a red build. Same antidote, and the same shape, as
 * {@link UpdateStategicGoalDtoIndicatorsContractTest}, which finding {@code A-124-01} introduced
 * in Phase 127; no new mechanism is invented here.
 */
class UpdateStategicGoalDtoPerspectiveContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("An absent perspective key deserializes to null -- absent means 'keep the current perspective'")
    void absentPerspectiveKeyDeserializesToNull() throws Exception {
        String body = "{\"title\":\"Objetivo sem a chave de perspetiva\","
                + "\"description\":\"Descrição\",\"weight\":1.00,\"year\":2026}";

        UpdateStategicGoalDTO dto = objectMapper.readValue(body, UpdateStategicGoalDTO.class);

        assertNull(dto.getPerspective(),
                "The perspective field must stay null when the key is absent. If this fails, the DTO "
                        + "field gained an initializer and the handler can no longer tell an omitted "
                        + "perspective from one the caller actually sent.");
    }

    @Test
    @DisplayName("A sent perspective key deserializes to the code -- the field exists and is bound")
    void sentPerspectiveKeyDeserializesToTheCode() throws Exception {
        String body = "{\"title\":\"Objetivo com perspetiva\","
                + "\"description\":\"Descrição\",\"weight\":1.00,\"year\":2026,"
                + "\"perspective\":\"FINANCIAL\"}";

        UpdateStategicGoalDTO dto = objectMapper.readValue(body, UpdateStategicGoalDTO.class);

        assertEquals("FINANCIAL", dto.getPerspective(),
                "The perspective the caller sent must reach the DTO. If this fails, the field was "
                        + "removed -- most likely by an iGRP Studio regeneration -- and finding A-124-02 "
                        + "is reopened: the server drops the field in silence and still answers 200.");
    }

    @Test
    @DisplayName("UpdateStategicGoalDTO.perspective is declared and has NO initializer")
    void updateDtoPerspectiveFieldIsDeclaredWithoutInitializer() throws Exception {
        Field field = UpdateStategicGoalDTO.class.getDeclaredField("perspective");
        assertEquals(String.class, field.getType());

        UpdateStategicGoalDTO fresh = new UpdateStategicGoalDTO();
        assertNull(fresh.getPerspective(),
                "A freshly constructed DTO must have a null perspective. A non-null value here means "
                        + "an initializer was put back on the field, which destroys the absent-vs-sent "
                        + "distinction before the handler ever runs.");
    }

    @Test
    @DisplayName("StategicGoalResponseDTO.incoherentLinks is declared, has NO initializer, and carries the warning")
    void responseDtoIncoherentLinksFieldIsDeclaredWithoutInitializer() throws Exception {
        Field field = StategicGoalResponseDTO.class.getDeclaredField("incoherentLinks");
        assertEquals(List.class, field.getType());

        StategicGoalResponseDTO fresh = new StategicGoalResponseDTO();
        assertNull(fresh.getIncoherentLinks(),
                "A freshly constructed response DTO must have a null incoherentLinks list, so that "
                        + "'there is nothing to warn about' is a state of its own rather than an empty "
                        + "list a caller has to interpret (rule 5 of the project CLAUDE.md).");

        IncoherentLinkDTO warning = new IncoherentLinkDTO();
        warning.setReason(IncoherentLinkDTO.Reason.CAUSE_EFFECT_ORDER_INVERTED);
        fresh.setIncoherentLinks(List.of(warning));

        assertNotNull(fresh.getIncoherentLinks());
        assertEquals(1, fresh.getIncoherentLinks().size());
    }

    @Test
    @DisplayName("A response with no warning serializes incoherentLinks as null, never as a list")
    void responseWithoutWarningSerializesTheFieldAsNull() throws Exception {
        StategicGoalResponseDTO response = new StategicGoalResponseDTO();
        response.setTitle("Objetivo sem avisos");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"incoherentLinks\":null"),
                "With nothing to warn about the field must serialize as null. If it serializes as [] "
                        + "the caller can no longer tell 'no incoherence' from 'the reading returned "
                        + "nothing', which is the indistinguishable-absence defect rule 5 of the project "
                        + "CLAUDE.md forbids. Actual body: " + json);
    }
}
