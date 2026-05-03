package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaPedidoAusenciaDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.PedidoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component("colabsGetMeLeaveRequestsQueryHandler")
@RequiredArgsConstructor
public class GetMeLeaveRequestsQueryHandler
        implements QueryHandler<GetMeLeaveRequestsQuery, ResponseEntity<WrapperListaPedidoAusenciaDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final PedidoAusenciaRepository pedidoAusenciaRepository;
    private final PedidoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPedidoAusenciaDTO> handle(GetMeLeaveRequestsQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var filter = new PedidoAusenciaFilter();
        filter.setFuncionarioId(UUID.fromString(funcionarioId.getStringValor()));
        filter.setEstado(query.getStatus());
        if (query.getLeaveTypeId() != null && !query.getLeaveTypeId().isBlank())
            filter.setTipoAusenciaId(UUID.fromString(query.getLeaveTypeId()));
        filter.setAno(query.getYear());

        var list = pedidoAusenciaRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaPedidoAusenciaDTO();
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
