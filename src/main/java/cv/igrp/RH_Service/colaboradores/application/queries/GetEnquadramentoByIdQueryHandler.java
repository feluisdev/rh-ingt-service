package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEnquadramentoByIdQueryHandler
        implements QueryHandler<GetEnquadramentoByIdQuery, ResponseEntity<EnquadramentoResponseDTO>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final FunctionRepository functionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpQueryHandler
    public ResponseEntity<EnquadramentoResponseDTO> handle(GetEnquadramentoByIdQuery query) {
        var e = enquadramentoRepository.findById(EnquadramentoId.from(query.getEnquadramentoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Enquadramento não encontrado: " + query.getEnquadramentoId()));
        var dto = mapper.toDTO(e);
        enrichNames(dto, e.getCareerId(), e.getCategoryId(), e.getGradeId(),
                e.getCargoId(), e.getFunctionId(), e.getUnidadeOrganicaId());
        return ResponseEntity.ok(dto);
    }

    private void enrichNames(EnquadramentoResponseDTO dto,
                             java.util.UUID careerId, java.util.UUID categoryId, java.util.UUID gradeId,
                             java.util.UUID cargoId, java.util.UUID functionId, java.util.UUID unitId) {
        if (careerId != null)
            careerRepository.findById(CareerId.from(careerId))
                    .ifPresent(c -> dto.setCareerName(c.getName()));
        if (categoryId != null)
            categoryRepository.findById(CategoryId.from(categoryId))
                    .ifPresent(c -> dto.setCategoryName(c.getName()));
        if (gradeId != null)
            gradeRepository.findById(GradeId.from(gradeId))
                    .ifPresent(g -> dto.setGradeName(g.getName()));
        if (cargoId != null)
            jobRepository.findById(JobId.from(cargoId))
                    .ifPresent(j -> dto.setCargoName(j.getName()));
        if (functionId != null)
            functionRepository.findById(FunctionId.from(functionId))
                    .ifPresent(f -> dto.setFunctionName(f.getName()));
        if (unitId != null)
            organizationalUnitRepository.findById(OrganizationalUnitId.from(unitId))
                    .ifPresent(u -> dto.setUnitName(u.getName()));
    }
}
