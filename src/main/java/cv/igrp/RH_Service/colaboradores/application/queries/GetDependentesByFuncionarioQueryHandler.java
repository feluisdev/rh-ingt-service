package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDependenteDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Component("colabsGetDependentesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetDependentesByFuncionarioQueryHandler
        implements QueryHandler<GetDependentesByFuncionarioQuery, ResponseEntity<WrapperListaDependenteDTO>> {

    private final DependenteRepository dependenteRepository;
    private final DependenteMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDependenteDTO> handle(GetDependentesByFuncionarioQuery query) {
        var items = dependenteRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(d -> Boolean.TRUE.equals(d.getIsActive())).toList();

        var rtCkeys = items.stream().map(d -> d.getRelationshipType()).filter(v -> v != null).collect(Collectors.toSet());
        Map<String, OptionDTO> rtMap = rtCkeys.isEmpty() ? Map.of() :
                optionLookupPort.findAllByCcodeAndCkeys(OptionCcode.RELATIONSHIP_TYPE.getCode(), rtCkeys);

        var list = items.stream().map(d -> {
            var dto = mapper.toDTO(d);
            if (d.getRelationshipType() != null) {
                OptionDTO opt = rtMap.get(d.getRelationshipType());
                if (opt != null) dto.setRelationshipTypeDesc(opt.cvalue());
            }
            return dto;
        }).toList();
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
