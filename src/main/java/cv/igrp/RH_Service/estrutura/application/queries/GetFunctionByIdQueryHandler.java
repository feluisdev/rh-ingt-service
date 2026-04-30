package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.FunctionResponse;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.FunctionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFunctionByIdQueryHandler
        implements QueryHandler<GetFunctionByIdQuery, ResponseEntity<FunctionResponse>> {

    private final FunctionRepository functionRepository;
    private final FunctionMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<FunctionResponse> handle(GetFunctionByIdQuery query) {
        var function = functionRepository.findById(FunctionId.from(query.getFunctionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Função não encontrada: " + query.getFunctionId()));

        return ResponseEntity.ok(mapper.toDTO(function));
    }
}
