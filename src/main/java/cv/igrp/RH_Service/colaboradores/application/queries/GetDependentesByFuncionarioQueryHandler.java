package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDependenteDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DependenteMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetDependentesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetDependentesByFuncionarioQueryHandler
        implements QueryHandler<GetDependentesByFuncionarioQuery, ResponseEntity<WrapperListaDependenteDTO>> {

    private final DependenteRepository dependenteRepository;
    private final DependenteMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDependenteDTO> handle(GetDependentesByFuncionarioQuery query) {
        var list = dependenteRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(d -> Boolean.TRUE.equals(d.getIsActive())).map(mapper::toDTO).toList();
        var wrapper = new WrapperListaDependenteDTO();
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
