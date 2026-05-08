package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaContratoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetContratosByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetContratosByFuncionarioQueryHandler
        implements QueryHandler<GetContratosByFuncionarioQuery, ResponseEntity<WrapperListaContratoDTO>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaContratoDTO> handle(GetContratosByFuncionarioQuery query) {
        var list = contratoRepository.findAllByFuncionarioIdOrderByStartDateDesc(FuncionarioId.from(query.getFuncionarioId()))
                .stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaContratoDTO();
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
