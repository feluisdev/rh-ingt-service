package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDadosBancariosDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetDadosBancariossByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetDadosBancariossByFuncionarioQueryHandler
        implements QueryHandler<GetDadosBancariossByFuncionarioQuery, ResponseEntity<WrapperListaDadosBancariosDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;
    private final DadosBancariosMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDadosBancariosDTO> handle(GetDadosBancariossByFuncionarioQuery query) {
        var list = dadosBancariosRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(d -> Boolean.TRUE.equals(d.getIsActive())).map(mapper::toDTO).toList();
        var wrapper = new WrapperListaDadosBancariosDTO();
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
