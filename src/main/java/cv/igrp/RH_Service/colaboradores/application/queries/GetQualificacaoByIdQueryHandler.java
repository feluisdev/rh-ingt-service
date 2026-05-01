package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.QualificacaoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetQualificacaoByIdQueryHandler")
@RequiredArgsConstructor
public class GetQualificacaoByIdQueryHandler
        implements QueryHandler<GetQualificacaoByIdQuery, ResponseEntity<QualificacaoResponse>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<QualificacaoResponse> handle(GetQualificacaoByIdQuery query) {
        var q = qualificacaoRepository.findById(QualificacaoId.from(query.getQualificacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Qualificação não encontrada: " + query.getQualificacaoId()));
        return ResponseEntity.ok(mapper.toDTO(q));
    }
}
