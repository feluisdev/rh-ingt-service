package cv.igrp.RH_Service.estrutura.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Cobertura de regressao para o bug do Mapa de Pessoal (2026-09-17): a listagem devolvia
 * {@code ocupado = null} em cada Lugar e {@code ocupados = 0, vagas = 0} no wrapper, campos
 * que o frontend lia como se fossem resposta -- mostrava Lugares providos como "Vago" e
 * oferecia-os no picker de admissao, onde rebentavam em 422.
 *
 * <p>O Lugar EXTINTO esta aqui de proposito: sai da {@code dotacao} (nao e uma cadeira do
 * quadro) mas continua a aparecer no {@code content} da listagem, que e o Mapa de Pessoal
 * completo. E o mesmo criterio de GetVagasListaUnidadeQueryHandler -- as duas APIs nao podem
 * discordar sobre a mesma unidade.
 */
@ExtendWith(MockitoExtension.class)
class GetPositionsByUnidadeQueryHandlerTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PositionOccupancyPort positionOccupancyPort;

    @Mock
    private PositionMapper mapper;

    @InjectMocks
    private GetPositionsByUnidadeQueryHandler handler;

    @Test
    void ocupacaoEContagensRefletemAsAfectacoesCorrentes() {
        UUID unidadeId = UUID.randomUUID();
        Position provido = lugar("LUG-001", Position.ATIVO, true);
        Position vago = lugar("LUG-002", Position.ATIVO, true);
        Position extinto = lugar("LUG-003", Position.EXTINTO, true);

        when(positionRepository.findByUnidade(unidadeId)).thenReturn(List.of(provido, vago, extinto));
        when(positionOccupancyPort.ocupados(anyCollection()))
                .thenReturn(Set.of(provido.getId().getValor()));
        when(mapper.toDTO(any(Position.class))).thenAnswer(inv -> {
            PositionResponseDTO dto = new PositionResponseDTO();
            dto.setNumeroLugar(((Position) inv.getArgument(0)).getNumeroLugar());
            return dto;
        });

        ResponseEntity<WrapperListaPositionDTO> response =
                handler.handle(new GetPositionsByUnidadeQuery(unidadeId.toString()));

        WrapperListaPositionDTO body = response.getBody();
        assertEquals(3, body.getTotalElements(), "o Mapa de Pessoal lista todos os Lugares");
        assertEquals(2, body.getDotacao(), "o Lugar EXTINTO nao conta para a dotacao");
        assertEquals(1, body.getOcupados());
        assertEquals(1, body.getVagas());

        assertTrue(dto(body, "LUG-001").getOcupado());
        assertFalse(dto(body, "LUG-002").getOcupado());
    }

    private static Position lugar(String numero, String estado, boolean active) {
        return Position.reconstituir(PositionId.gerarNovo(), numero, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                estado, null, active);
    }

    private static PositionResponseDTO dto(WrapperListaPositionDTO body, String numero) {
        return body.getContent().stream()
                .filter(d -> numero.equals(d.getNumeroLugar()))
                .findFirst()
                .orElseThrow();
    }
}
