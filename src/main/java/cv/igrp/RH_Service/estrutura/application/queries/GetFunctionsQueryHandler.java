package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaFunctionDTO;
import cv.igrp.RH_Service.estrutura.domain.filter.FunctionFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.FunctionMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class GetFunctionsQueryHandler
        implements QueryHandler<GetFunctionsQuery, ResponseEntity<WrapperListaFunctionDTO>> {

    private final FunctionRepository functionRepository;
    private final FunctionMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFunctionDTO> handle(GetFunctionsQuery query) {
        var filter = new FunctionFilter();
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = functionRepository.findAll(filter).stream()
                .map(mapper::toDTO)
                .toList();

        var wrapper = new WrapperListaFunctionDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(content.size());

        return ResponseEntity.ok(wrapper);
    }
}
