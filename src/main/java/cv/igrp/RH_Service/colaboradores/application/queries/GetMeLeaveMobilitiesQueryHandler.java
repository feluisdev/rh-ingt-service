package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaLicencaMobilidadeDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
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

@Component("colabsGetMeLeaveMobilitiesQueryHandler")
@RequiredArgsConstructor
public class GetMeLeaveMobilitiesQueryHandler
        implements QueryHandler<GetMeLeaveMobilitiesQuery, ResponseEntity<WrapperListaLicencaMobilidadeDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaLicencaMobilidadeDTO> handle(GetMeLeaveMobilitiesQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var filter = new LicencaMobilidadeFilter();
        filter.setFuncionarioId(UUID.fromString(funcionarioId.getStringValor()));

        var list = licencaRepository.findAllByFuncionarioId(funcionarioId, filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaLicencaMobilidadeDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        return ResponseEntity.ok(wrapper);
    }
}
