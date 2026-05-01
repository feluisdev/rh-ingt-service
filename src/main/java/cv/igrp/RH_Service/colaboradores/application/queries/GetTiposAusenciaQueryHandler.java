package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaTipoAusenciaDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.TipoAusenciaMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetTiposAusenciaQueryHandler")
@RequiredArgsConstructor
public class GetTiposAusenciaQueryHandler
        implements QueryHandler<GetTiposAusenciaQuery, ResponseEntity<WrapperListaTipoAusenciaDTO>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final TipoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaTipoAusenciaDTO> handle(GetTiposAusenciaQuery query) {
        var filter = new TipoAusenciaFilter();
        filter.setActive(query.getActive());
        var list = tipoAusenciaRepository.findAll(filter).stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaTipoAusenciaDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
