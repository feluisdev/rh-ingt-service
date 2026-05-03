package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ProcessoDisciplinarMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetProcessoDisciplinarQueryHandler")
@RequiredArgsConstructor
public class GetProcessoDisciplinarQueryHandler
        implements QueryHandler<GetProcessoDisciplinarQuery, ResponseEntity<ProcessoDisciplinarDTO>> {

    private final ProcessoDisciplinarRepository processoDisciplinarRepository;
    private final ProcessoDisciplinarMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<ProcessoDisciplinarDTO> handle(GetProcessoDisciplinarQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var processo = processoDisciplinarRepository.findById(ProcessoDisciplinarId.from(query.getProcessoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Processo disciplinar não encontrado: " + query.getProcessoId()));

        if (!processo.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound(
                    "Processo disciplinar não encontrado: " + query.getProcessoId());

        return ResponseEntity.ok(mapper.toDTO(processo));
    }
}
