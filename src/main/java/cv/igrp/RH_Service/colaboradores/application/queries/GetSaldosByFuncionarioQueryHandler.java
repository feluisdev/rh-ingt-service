package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaSaldoAusenciaDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.SaldoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SaldoAusenciaMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetSaldosByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetSaldosByFuncionarioQueryHandler
        implements QueryHandler<GetSaldosByFuncionarioQuery, ResponseEntity<WrapperListaSaldoAusenciaDTO>> {

    private final SaldoAusenciaRepository saldoRepository;
    private final SaldoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaSaldoAusenciaDTO> handle(GetSaldosByFuncionarioQuery query) {
        var filter = new SaldoAusenciaFilter();
        filter.setFuncionarioId(FuncionarioId.from(query.getFuncionarioId()).getValor());
        filter.setAno(query.getAno());
        filter.setTipoAusenciaId(query.getTipoAusenciaId());
        var list = saldoRepository
                .findAllByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()), filter)
                .stream().map(mapper::toDTO).toList();
        var wrapper = new WrapperListaSaldoAusenciaDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
