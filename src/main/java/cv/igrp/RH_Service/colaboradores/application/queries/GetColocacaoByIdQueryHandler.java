package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ColocacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetColocacaoByIdQueryHandler")
@RequiredArgsConstructor
public class GetColocacaoByIdQueryHandler
        implements QueryHandler<GetColocacaoByIdQuery, ResponseEntity<ColocacaoResponseDTO>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final JobRepository jobRepository;

    @IgrpQueryHandler
    public ResponseEntity<ColocacaoResponseDTO> handle(GetColocacaoByIdQuery query) {
        var colocacao = colocacaoRepository.findById(ColocacaoId.from(query.getColocacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Colocação não encontrada: " + query.getColocacaoId()));
        var dto = mapper.toDTO(colocacao);
        if (colocacao.getUnitId() != null)
            organizationalUnitRepository.findById(OrganizationalUnitId.from(colocacao.getUnitId()))
                    .ifPresent(u -> dto.setUnitName(u.getName()));
        if (colocacao.getJobId() != null)
            jobRepository.findById(JobId.from(colocacao.getJobId()))
                    .ifPresent(j -> dto.setJobName(j.getName()));
        return ResponseEntity.ok(dto);
    }
}
