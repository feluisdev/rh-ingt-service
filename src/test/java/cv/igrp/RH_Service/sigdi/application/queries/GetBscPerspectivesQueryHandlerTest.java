package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GetBscPerspectivesQueryHandlerTest {

    @Mock
    private BscPerspectiveConfigRepository repository;

    @InjectMocks
    private GetBscPerspectivesQueryHandler handler;

    @Test
    void handleReturnsAllFourPerspectivesSortedByDisplayOrder() {
        List<BscPerspectiveConfig> configs = List.of(
                BscPerspectiveConfig.reconstruct(UUID.randomUUID(), "FINANCIAL", "Financeira", 1),
                BscPerspectiveConfig.reconstruct(UUID.randomUUID(), "CUSTOMER", "Cliente / Mercado", 2),
                BscPerspectiveConfig.reconstruct(UUID.randomUUID(), "PROCESS", "Processos Internos", 3),
                BscPerspectiveConfig.reconstruct(UUID.randomUUID(), "LEARNING", "Aprendizagem e Crescimento", 4));
        when(repository.findAll()).thenReturn(configs);

        ResponseEntity<List<BscPerspectiveItemDTO>> response = handler.handle(new GetBscPerspectivesQuery());

        assertEquals(200, response.getStatusCode().value());
        List<BscPerspectiveItemDTO> body = response.getBody();
        assertEquals(4, body.size());
        assertEquals(List.of(1, 2, 3, 4), body.stream().map(BscPerspectiveItemDTO::getOrder).toList());
        assertEquals("FINANCIAL", body.get(0).getCode());
        assertEquals("Financeira", body.get(0).getLabel());
        assertEquals("LEARNING", body.get(3).getCode());
        assertEquals(4, body.get(3).getOrder());
    }
}
