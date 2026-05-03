package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaEnquadramentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class GetEnquadramentosHistoricoQueryHandler
        implements QueryHandler<GetEnquadramentosHistoricoQuery, ResponseEntity<WrapperListaEnquadramentoDTO>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaEnquadramentoDTO> handle(GetEnquadramentosHistoricoQuery query) {
        var list = enquadramentoRepository.findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId.from(query.getFuncionarioId()))
                .stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaEnquadramentoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        wrapper.setPageNumber(0);
        wrapper.setPageSize(list.size());
        wrapper.setTotalPages(list.size() == 0 ? 0 : 1);
        wrapper.setFirst(true);
        wrapper.setLast(true);
        return ResponseEntity.ok(wrapper);
    }
}
