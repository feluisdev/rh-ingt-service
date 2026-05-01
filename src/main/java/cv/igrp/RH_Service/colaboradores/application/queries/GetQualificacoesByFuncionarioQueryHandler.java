package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaQualificacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetQualificacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetQualificacoesByFuncionarioQueryHandler
        implements QueryHandler<GetQualificacoesByFuncionarioQuery, ResponseEntity<WrapperListaQualificacaoDTO>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaQualificacaoDTO> handle(GetQualificacoesByFuncionarioQuery query) {
        var list = qualificacaoRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(q -> Boolean.TRUE.equals(q.getIsActive())).map(mapper::toDTO).toList();
        var wrapper = new WrapperListaQualificacaoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
