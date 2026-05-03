package cv.igrp.RH_Service.parametrizacoes.interfaces.rest;

import cv.igrp.RH_Service.parametrizacoes.application.commands.CreateOptionCommandHandler;
import cv.igrp.RH_Service.parametrizacoes.application.commands.DesativarOptionCommandHandler;
import cv.igrp.RH_Service.parametrizacoes.application.commands.AtivarOptionCommandHandler;
import cv.igrp.RH_Service.parametrizacoes.application.commands.UpdateOptionCommandHandler;
import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaOptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.queries.GetOptionQueryHandler;
import cv.igrp.RH_Service.parametrizacoes.application.queries.ListOptionsQueryHandler;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferenceOptionsController.class)
class ReferenceOptionsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandBus commandBus;

    @MockBean
    private QueryBus queryBus;

    @Test
    void getOptions_deveRetornar200() throws Exception {
        var wrapper = new WrapperListaOptionDTO();
        wrapper.setContent(new ArrayList<>());
        wrapper.setTotalElements(0L);

        when(queryBus.handle(any())).thenReturn(ResponseEntity.ok(wrapper));

        mockMvc.perform(get("/api/v1/rh/reference/options")
                .param("pagina", "0")
                .param("tamanho", "20"))
            .andExpect(status().isOk());
    }

    @Test
    void getOptionById_comIdInexistente_deveRetornar404() throws Exception {
        when(queryBus.handle(any()))
            .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/api/v1/rh/reference/options/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void createOption_comPayloadValido_deveRetornar201() throws Exception {
        String optionId = UUID.randomUUID().toString();
        when(commandBus.send(any()))
            .thenReturn(ResponseEntity.status(201).body(Map.of("optionId", optionId)));

        String payload = """
            {
              "ccode": "SEX",
              "ckey": "M",
              "cvalue": "Masculino",
              "locale": "pt-CV"
            }
            """;

        mockMvc.perform(post("/api/v1/rh/reference/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());
    }

    @Test
    void createOption_semCcodeObrigatorio_deveRetornar400() throws Exception {
        String payload = """
            {
              "ckey": "M",
              "cvalue": "Masculino"
            }
            """;

        mockMvc.perform(post("/api/v1/rh/reference/options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deleteOption_deveRetornar200() throws Exception {
        when(commandBus.send(any()))
            .thenReturn(ResponseEntity.ok(Map.of("message", "Etiqueta desactivada com sucesso")));

        mockMvc.perform(delete("/api/v1/rh/reference/options/" + UUID.randomUUID()))
            .andExpect(status().isOk());
    }

    @Test
    void activateOption_deveRetornar200() throws Exception {
        when(commandBus.send(any()))
            .thenReturn(ResponseEntity.ok(Map.of("message", "Etiqueta activada com sucesso")));

        mockMvc.perform(patch("/api/v1/rh/reference/options/" + UUID.randomUUID() + "/activate"))
            .andExpect(status().isOk());
    }
}
