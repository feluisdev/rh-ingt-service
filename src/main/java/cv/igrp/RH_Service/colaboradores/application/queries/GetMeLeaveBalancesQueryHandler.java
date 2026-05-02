package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaSaldoAusenciaDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.SaldoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SaldoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

@Component("colabsGetMeLeaveBalancesQueryHandler")
@RequiredArgsConstructor
public class GetMeLeaveBalancesQueryHandler
        implements QueryHandler<GetMeLeaveBalancesQuery, ResponseEntity<WrapperListaSaldoAusenciaDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final SaldoAusenciaRepository saldoAusenciaRepository;
    private final SaldoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaSaldoAusenciaDTO> handle(GetMeLeaveBalancesQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var filter = new SaldoAusenciaFilter();
        filter.setFuncionarioId(UUID.fromString(funcionarioId.getStringValor()));
        filter.setAno(LocalDate.now().getYear());

        var list = saldoAusenciaRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaSaldoAusenciaDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
