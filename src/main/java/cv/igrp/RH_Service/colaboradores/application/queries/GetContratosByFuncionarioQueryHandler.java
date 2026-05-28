package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaContratoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
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

@Component("colabsGetContratosByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetContratosByFuncionarioQueryHandler
        implements QueryHandler<GetContratosByFuncionarioQuery, ResponseEntity<WrapperListaContratoDTO>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;
    private final ContractTypeRepository contractTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaContratoDTO> handle(GetContratosByFuncionarioQuery query) {
        var items = contratoRepository.findAllByFuncionarioIdOrderByStartDateDesc(FuncionarioId.from(query.getFuncionarioId()));

        var ctIds = items.stream().map(c -> c.getContractTypeId()).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> ctNames = ctIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> contractTypeRepository.findById(ContractTypeId.from(id)).map(ct -> ct.getDescription()).orElse(null),
                        (a, b) -> a));

        var list = items.stream().map(c -> {
            var dto = mapper.toDTO(c);
            if (c.getContractTypeId() != null) dto.setContractTypeName(ctNames.get(c.getContractTypeId()));
            return dto;
        }).toList();

        var wrapper = new WrapperListaContratoDTO();
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
