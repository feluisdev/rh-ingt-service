package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaColocacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.ColocacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
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

@Component("colabsGetColocacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetColocacoesByFuncionarioQueryHandler
        implements QueryHandler<GetColocacoesByFuncionarioQuery, ResponseEntity<WrapperListaColocacaoDTO>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final JobRepository jobRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaColocacaoDTO> handle(GetColocacoesByFuncionarioQuery query) {
        var filter = new ColocacaoFilter();
        filter.setIsCurrent(query.getIsCurrent());

        var items = colocacaoRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter);

        var unitIds = items.stream().map(c -> c.getUnitId()).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> unitNames = unitIds.isEmpty() ? Map.of() :
                organizationalUnitRepository.findAllByIds(unitIds).stream()
                        .collect(Collectors.toMap(u -> u.getId().getValor(), u -> u.getName()));

        var jobIds = items.stream().map(c -> c.getJobId()).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> jobNames = jobIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> jobRepository.findById(JobId.from(id)).map(j -> j.getName()).orElse(null),
                        (a, b) -> a));

        var list = items.stream().map(c -> {
            var dto = mapper.toDTO(c);
            if (c.getUnitId() != null) dto.setUnitName(unitNames.get(c.getUnitId()));
            if (c.getJobId() != null) dto.setJobName(jobNames.get(c.getJobId()));
            return dto;
        }).toList();

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
