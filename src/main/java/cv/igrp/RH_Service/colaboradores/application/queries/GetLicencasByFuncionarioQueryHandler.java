package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaLicencaMobilidadeDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("colabsGetLicencasByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetLicencasByFuncionarioQueryHandler
        implements QueryHandler<GetLicencasByFuncionarioQuery, ResponseEntity<WrapperListaLicencaMobilidadeDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaMobilidadeMapper mapper;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaLicencaMobilidadeDTO> handle(GetLicencasByFuncionarioQuery query) {
        var filter = new LicencaMobilidadeFilter();
        filter.setFuncionarioId(FuncionarioId.from(query.getFuncionarioId()).getValor());
        filter.setActive(query.getActive());
        filter.setSubtipoId(query.getSubtipoId());
        var items = licencaRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter);

        var unitIds = items.stream().map(l -> l.getDestinationUnitId()).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> unitNames = unitIds.isEmpty() ? Map.of() :
                organizationalUnitRepository.findAllByIds(unitIds).stream()
                        .collect(Collectors.toMap(u -> u.getId().getValor(), u -> u.getName()));

        var list = items.stream().map(l -> {
            var dto = mapper.toDTO(l);
            if (l.getDestinationUnitId() != null) dto.setDestinationUnitName(unitNames.get(l.getDestinationUnitId()));
            return dto;
        }).toList();

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
