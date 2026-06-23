package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaFormacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.FormacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FormacaoMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Component("colabsGetFormacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetFormacoesByFuncionarioQueryHandler
        implements QueryHandler<GetFormacoesByFuncionarioQuery, ResponseEntity<WrapperListaFormacaoDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FormacaoRepository formacaoRepository;
    private final FormacaoMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaFormacaoDTO> handle(GetFormacoesByFuncionarioQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        var filter = new FormacaoFilter();
        filter.setYear(query.getYear());

        var items = formacaoRepository.findAllByFuncionarioId(funcionarioId, filter);

        var ttCkeys = items.stream().map(f -> f.getTrainingType()).filter(v -> v != null).collect(Collectors.toSet());
        Map<String, OptionDTO> ttMap = ttCkeys.isEmpty() ? Map.of() :
                optionLookupPort.findAllByCcodeAndCkeys(OptionCcode.TRAINING_TYPE.getCode(), ttCkeys);

        var list = items.stream().map(f -> {
            var dto = mapper.toDTO(f);
            if (f.getTrainingType() != null) {
                OptionDTO opt = ttMap.get(f.getTrainingType());
                if (opt != null) dto.setTrainingTypeDesc(opt.cvalue());
            }
            return dto;
        }).toList();

        var wrapper = new WrapperListaFormacaoDTO();
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
