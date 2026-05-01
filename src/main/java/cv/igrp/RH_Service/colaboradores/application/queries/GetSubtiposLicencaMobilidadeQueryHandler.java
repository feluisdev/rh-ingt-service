package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaSubtipoLicencaMobilidadeDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.SubtipoLicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SubtipoLicencaMobilidadeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetSubtiposLicencaMobilidadeQueryHandler")
@RequiredArgsConstructor
public class GetSubtiposLicencaMobilidadeQueryHandler
        implements QueryHandler<GetSubtiposLicencaMobilidadeQuery, ResponseEntity<WrapperListaSubtipoLicencaMobilidadeDTO>> {

    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final SubtipoLicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaSubtipoLicencaMobilidadeDTO> handle(GetSubtiposLicencaMobilidadeQuery query) {
        var filter = new SubtipoLicencaMobilidadeFilter();
        filter.setActive(query.getActive());
        filter.setRecordType(query.getRecordType());
        var list = subtipoRepository.findAll(filter).stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaSubtipoLicencaMobilidadeDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
