package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaPedidoAusenciaDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.PedidoAusenciaMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetPedidosByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetPedidosByFuncionarioQueryHandler
        implements QueryHandler<GetPedidosByFuncionarioQuery, ResponseEntity<WrapperListaPedidoAusenciaDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final PedidoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPedidoAusenciaDTO> handle(GetPedidosByFuncionarioQuery query) {
        var filter = new PedidoAusenciaFilter();
        filter.setFuncionarioId(FuncionarioId.from(query.getFuncionarioId()).getValor());
        filter.setEstado(query.getEstado());
        filter.setTipoAusenciaId(query.getTipoAusenciaId());
        filter.setAno(query.getAno());
        var list = pedidoRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter)
                .stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaPedidoAusenciaDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
