package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDadosBancariosDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
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

@Component("colabsGetDadosBancariossByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetDadosBancariossByFuncionarioQueryHandler
        implements QueryHandler<GetDadosBancariossByFuncionarioQuery, ResponseEntity<WrapperListaDadosBancariosDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;
    private final DadosBancariosMapper mapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDadosBancariosDTO> handle(GetDadosBancariossByFuncionarioQuery query) {
        var items = dadosBancariosRepository.findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .stream().filter(d -> Boolean.TRUE.equals(d.getIsActive())).toList();

        var bancoCkeys = items.stream().map(d -> d.getBanco()).filter(v -> v != null).collect(Collectors.toSet());
        Map<String, OptionDTO> bancoMap = bancoCkeys.isEmpty() ? Map.of() :
                optionLookupPort.findAllByCcodeAndCkeys(OptionCcode.BANCO.getCode(), bancoCkeys);

        var list = items.stream().map(d -> {
            var dto = mapper.toDTO(d);
            if (d.getBanco() != null) {
                OptionDTO opt = bancoMap.get(d.getBanco());
                if (opt != null) dto.setBancoDesc(opt.cvalue());
            }
            return dto;
        }).toList();
        var wrapper = new WrapperListaDadosBancariosDTO();
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
