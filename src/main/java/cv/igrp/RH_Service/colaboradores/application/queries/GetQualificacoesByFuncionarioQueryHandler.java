package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaQualificacaoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Component("colabsGetQualificacoesByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetQualificacoesByFuncionarioQueryHandler
        implements QueryHandler<GetQualificacoesByFuncionarioQuery, ResponseEntity<WrapperListaQualificacaoDTO>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaQualificacaoDTO> handle(GetQualificacoesByFuncionarioQuery query) {
        var items = qualificacaoRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(q -> Boolean.TRUE.equals(q.getIsActive())).toList();

        var levelCkeys = items.stream().map(q -> q.getLevel()).filter(v -> v != null).collect(Collectors.toSet());
        Map<String, OptionDTO> levelMap = levelCkeys.isEmpty() ? Map.of() :
                optionLookupPort.findAllByCcodeAndCkeys(OptionCcode.QUALIFICATION_LEVEL.getCode(), levelCkeys);

        var countryCkeys = items.stream().map(q -> q.getCountry()).filter(v -> v != null).collect(Collectors.toSet());
        Map<String, OptionDTO> countryMap = countryCkeys.isEmpty() ? Map.of() :
                optionLookupPort.findAllByCcodeAndCkeys(OptionCcode.NATIONALITY.getCode(), countryCkeys);

        var list = items.stream().map(q -> {
            var dto = mapper.toDTO(q);
            if (q.getLevel() != null) {
                OptionDTO opt = levelMap.get(q.getLevel());
                if (opt != null) dto.setLevelDesc(opt.cvalue());
            }
            if (q.getCountry() != null) {
                OptionDTO opt = countryMap.get(q.getCountry());
                if (opt != null) dto.setCountryDesc(opt.cvalue());
            }
            return dto;
        }).toList();
        var wrapper = new WrapperListaQualificacaoDTO();
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
