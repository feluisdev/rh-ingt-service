package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEnquadramentoAtualQueryHandler
        implements QueryHandler<GetEnquadramentoAtualQuery, ResponseEntity<EnquadramentoResponse>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<EnquadramentoResponse> handle(GetEnquadramentoAtualQuery query) {
        var e = enquadramentoRepository.findCurrentByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Enquadramento actual não encontrado para funcionário: " + query.getFuncionarioId()));
        return ResponseEntity.ok(mapper.toDTO(e));
    }
}
