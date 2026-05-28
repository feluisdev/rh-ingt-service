package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetQualificacaoByIdQueryHandler")
@RequiredArgsConstructor
public class GetQualificacaoByIdQueryHandler
        implements QueryHandler<GetQualificacaoByIdQuery, ResponseEntity<QualificacaoResponseDTO>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<QualificacaoResponseDTO> handle(GetQualificacaoByIdQuery query) {
        var q = qualificacaoRepository.findById(QualificacaoId.from(query.getQualificacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Qualificação não encontrada: " + query.getQualificacaoId()));
        var dto = mapper.toDTO(q);
        if (q.getLevel() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.QUALIFICATION_LEVEL.getCode(), q.getLevel())
                    .ifPresent(opt -> dto.setLevelDesc(opt.cvalue()));
        }
        if (q.getCountry() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.NATIONALITY.getCode(), q.getCountry())
                    .ifPresent(opt -> dto.setCountryDesc(opt.cvalue()));
        }
        return ResponseEntity.ok(dto);
    }
}
