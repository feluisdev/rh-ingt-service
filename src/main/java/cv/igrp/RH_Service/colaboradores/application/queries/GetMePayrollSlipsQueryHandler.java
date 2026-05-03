package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaReciboDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.ReciboFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReciboVencimentoRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ReciboVencimentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetMePayrollSlipsQueryHandler")
@RequiredArgsConstructor
public class GetMePayrollSlipsQueryHandler
        implements QueryHandler<GetMePayrollSlipsQuery, ResponseEntity<WrapperListaReciboDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final ReciboVencimentoRepository reciboVencimentoRepository;
    private final ReciboVencimentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaReciboDTO> handle(GetMePayrollSlipsQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var filter = new ReciboFilter();
        filter.setPeriodYear(query.getPeriodYear());
        filter.setPeriodMonth(query.getPeriodMonth());

        var list = reciboVencimentoRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaReciboDTO();
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
