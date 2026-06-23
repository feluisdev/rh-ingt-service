package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFuncionarioDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("colabsGetFuncionariosQueryHandler")
@RequiredArgsConstructor
public class GetFuncionariosQueryHandler
        implements QueryHandler<GetFuncionariosQuery, ResponseEntity<WrapperListaFuncionarioDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper mapper;
    private final WorkerStateRepository workerStateRepository;
    private final DocumentTypeRepository documentTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFuncionarioDTO> handle(GetFuncionariosQuery query) {
        var filter = new FuncionarioFilter();
        filter.setNome(query.getNome());
        filter.setNif(query.getNif());
        if (query.getWorkerStateId() != null && !query.getWorkerStateId().isBlank()) {
            filter.setWorkerStateId(UUID.fromString(query.getWorkerStateId()));
        }
        if (query.getUnidadeOrganicaId() != null && !query.getUnidadeOrganicaId().isBlank()) {
            filter.setUnidadeOrganicaId(UUID.fromString(query.getUnidadeOrganicaId()));
        }
        if (query.getCareerId() != null && !query.getCareerId().isBlank()) {
            filter.setCareerId(UUID.fromString(query.getCareerId()));
        }
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var wsFilter = new WorkerStateFilter();
        wsFilter.setPage(0);
        wsFilter.setSize(100);
        Map<UUID, String> wsNameMap = workerStateRepository.findAll(wsFilter).getData().stream()
                .collect(Collectors.toMap(ws -> ws.getId().getValor(), ws -> ws.getDescription()));

        var funcionarios = funcionarioRepository.findAll(filter);

        var dtIds = funcionarios.stream().map(f -> f.getDocumentTypeId())
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<UUID, String> dtNameMap = dtIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> documentTypeRepository.findById(DocumentTypeId.from(id)).map(dt -> dt.getDescricao()).orElse(null),
                        (a, b) -> a));

        var content = funcionarios.stream()
                .map(f -> {
                    var dto = mapper.toDTO(f);
                    if (f.getWorkerStateId() != null) {
                        String name = wsNameMap.get(f.getWorkerStateId());
                        if (name != null) dto.setWorkerStateName(name);
                    }
                    if (f.getDocumentTypeId() != null) dto.setDocumentTypeName(dtNameMap.get(f.getDocumentTypeId()));
                    return dto;
                }).toList();
        long total = funcionarioRepository.countAll(filter);

        var wrapper = new WrapperListaFuncionarioDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(total);
        wrapper.setPageNumber(filter.getPage());
        wrapper.setPageSize(filter.getSize());
        wrapper.setTotalPages((int) Math.ceil((double) total / filter.getSize()));

        return ResponseEntity.ok(wrapper);
    }
}
