package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFuncionarioDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component("colabsGetFuncionariosQueryHandler")
@RequiredArgsConstructor
public class GetFuncionariosQueryHandler
        implements QueryHandler<GetFuncionariosQuery, ResponseEntity<WrapperListaFuncionarioDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFuncionarioDTO> handle(GetFuncionariosQuery query) {
        var filter = new FuncionarioFilter();
        filter.setNome(query.getNome());
        filter.setNif(query.getNif());
        filter.setSituacaoProfissional(query.getSituacaoProfissional());
        if (query.getUnidadeOrganicaId() != null && !query.getUnidadeOrganicaId().isBlank()) {
            filter.setUnidadeOrganicaId(UUID.fromString(query.getUnidadeOrganicaId()));
        }
        if (query.getCareerId() != null && !query.getCareerId().isBlank()) {
            filter.setCareerId(UUID.fromString(query.getCareerId()));
        }
        filter.setIsActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = funcionarioRepository.findAll(filter).stream()
                .map(mapper::toDTO).toList();
        long total = funcionarioRepository.countAll(filter);

        var wrapper = new WrapperListaFuncionarioDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(total);

        return ResponseEntity.ok(wrapper);
    }
}
