package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaMobilidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetMeLicencaMobilidadeByIdQueryHandler")
@RequiredArgsConstructor
public class GetMeLicencaMobilidadeByIdQueryHandler
        implements QueryHandler<GetMeLicencaMobilidadeByIdQuery, ResponseEntity<LicencaMobilidadeResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<LicencaMobilidadeResponseDTO> handle(GetMeLicencaMobilidadeByIdQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(query.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Licença/mobilidade não encontrada: " + query.getLicencaId()));

        if (!licenca.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Licença/mobilidade não encontrada: " + query.getLicencaId());

        return ResponseEntity.ok(mapper.toDTO(licenca));
    }
}
