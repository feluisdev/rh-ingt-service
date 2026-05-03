package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PedidoAusenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.PedidoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetPedidoAusenciaByIdQueryHandler")
@RequiredArgsConstructor
public class GetPedidoAusenciaByIdQueryHandler
        implements QueryHandler<GetPedidoAusenciaByIdQuery, ResponseEntity<PedidoAusenciaResponseDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final PedidoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<PedidoAusenciaResponseDTO> handle(GetPedidoAusenciaByIdQuery query) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(query.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + query.getPedidoId()));
        return ResponseEntity.ok(mapper.toDTO(pedido));
    }
}
