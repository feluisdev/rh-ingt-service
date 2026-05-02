package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFormacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.FormacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FormacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetFormacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetFormacoesByFuncionarioQueryHandler
        implements QueryHandler<GetFormacoesByFuncionarioQuery, ResponseEntity<WrapperListaFormacaoDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FormacaoRepository formacaoRepository;
    private final FormacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFormacaoDTO> handle(GetFormacoesByFuncionarioQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        var filter = new FormacaoFilter();
        filter.setYear(query.getYear());

        var list = formacaoRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaFormacaoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
