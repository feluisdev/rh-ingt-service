package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFeriadoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.FeriadoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FeriadoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetFeriadosQueryHandler")
@RequiredArgsConstructor
public class GetFeriadosQueryHandler
        implements QueryHandler<GetFeriadosQuery, ResponseEntity<WrapperListaFeriadoDTO>> {

    private final FeriadoRepository feriadoRepository;
    private final FeriadoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFeriadoDTO> handle(GetFeriadosQuery query) {
        var filter = new FeriadoFilter();
        filter.setAno(query.getAno());
        filter.setIsNational(query.getIsNational());
        filter.setActive(query.getActive());
        var list = feriadoRepository.findAll(filter).stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaFeriadoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
