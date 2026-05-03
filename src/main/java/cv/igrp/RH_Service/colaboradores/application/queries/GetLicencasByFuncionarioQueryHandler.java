package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaLicencaMobilidadeDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetLicencasByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetLicencasByFuncionarioQueryHandler
        implements QueryHandler<GetLicencasByFuncionarioQuery, ResponseEntity<WrapperListaLicencaMobilidadeDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaLicencaMobilidadeDTO> handle(GetLicencasByFuncionarioQuery query) {
        var filter = new LicencaMobilidadeFilter();
        filter.setFuncionarioId(FuncionarioId.from(query.getFuncionarioId()).getValor());
        filter.setActive(query.getActive());
        filter.setSubtipoId(query.getSubtipoId());
        var list = licencaRepository
                .findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter)
                .stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaLicencaMobilidadeDTO();
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
