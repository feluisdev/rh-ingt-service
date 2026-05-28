package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FormacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FormacaoMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetFormacaoQueryHandler")
@RequiredArgsConstructor
public class GetFormacaoQueryHandler
        implements QueryHandler<GetFormacaoQuery, ResponseEntity<FormacaoDTO>> {

    private final FormacaoRepository formacaoRepository;
    private final FormacaoMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<FormacaoDTO> handle(GetFormacaoQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var formacao = formacaoRepository.findById(FormacaoId.from(query.getFormacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Formação não encontrada: " + query.getFormacaoId()));

        if (!formacao.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Formação não encontrada: " + query.getFormacaoId());

        var dto = mapper.toDTO(formacao);
        if (formacao.getTrainingType() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.TRAINING_TYPE.getCode(), formacao.getTrainingType())
                    .ifPresent(opt -> dto.setTrainingTypeDesc(opt.cvalue()));
        }
        return ResponseEntity.ok(dto);
    }
}
