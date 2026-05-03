package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaProcessoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ProcessoDisciplinarMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetProcessosDisciplinaresByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetProcessosDisciplinaresByFuncionarioQueryHandler
        implements QueryHandler<GetProcessosDisciplinaresByFuncionarioQuery, ResponseEntity<WrapperListaProcessoDisciplinarDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ProcessoDisciplinarRepository processoDisciplinarRepository;
    private final ProcessoDisciplinarMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaProcessoDisciplinarDTO> handle(GetProcessosDisciplinaresByFuncionarioQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        var list = processoDisciplinarRepository.findAllByFuncionarioId(funcionarioId)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaProcessoDisciplinarDTO();
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
