package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaColocacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.ColocacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetColocacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetColocacoesByFuncionarioQueryHandler
        implements QueryHandler<GetColocacoesByFuncionarioQuery, ResponseEntity<WrapperListaColocacaoDTO>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaColocacaoDTO> handle(GetColocacoesByFuncionarioQuery query) {
        var filter = new ColocacaoFilter();
        filter.setIsCurrent(query.getIsCurrent());

        var list = colocacaoRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaColocacaoDTO();
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
