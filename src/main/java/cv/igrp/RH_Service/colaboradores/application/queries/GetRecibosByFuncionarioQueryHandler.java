package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaReciboDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.ReciboFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReciboVencimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ReciboVencimentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetRecibosByFuncionarioQueryHandler")
@RequiredArgsConstructor
public class GetRecibosByFuncionarioQueryHandler
        implements QueryHandler<GetRecibosByFuncionarioQuery, ResponseEntity<WrapperListaReciboDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ReciboVencimentoRepository reciboVencimentoRepository;
    private final ReciboVencimentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaReciboDTO> handle(GetRecibosByFuncionarioQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        var filter = new ReciboFilter();
        filter.setPeriodYear(query.getPeriodYear());
        filter.setPeriodMonth(query.getPeriodMonth());

        var list = reciboVencimentoRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaReciboDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
