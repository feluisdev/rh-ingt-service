package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
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
public class GetEnquadramentoAtualQueryHandler
        implements QueryHandler<GetEnquadramentoAtualQuery, ResponseEntity<EnquadramentoResponseDTO>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final FunctionRepository functionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpQueryHandler
    public ResponseEntity<EnquadramentoResponseDTO> handle(GetEnquadramentoAtualQuery query) {
        var e = enquadramentoRepository.findCurrentByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Enquadramento actual não encontrado para funcionário: " + query.getFuncionarioId()));
        var dto = mapper.toDTO(e);
        if (e.getCareerId() != null)
            careerRepository.findById(CareerId.from(e.getCareerId()))
                    .ifPresent(c -> dto.setCareerName(c.getName()));
        if (e.getCategoryId() != null)
            categoryRepository.findById(CategoryId.from(e.getCategoryId()))
                    .ifPresent(c -> dto.setCategoryName(c.getName()));
        if (e.getGradeId() != null)
            gradeRepository.findById(GradeId.from(e.getGradeId()))
                    .ifPresent(g -> dto.setGradeName(g.getName()));
        if (e.getCargoId() != null)
            jobRepository.findById(JobId.from(e.getCargoId()))
                    .ifPresent(j -> dto.setCargoName(j.getName()));
        if (e.getFunctionId() != null)
            functionRepository.findById(FunctionId.from(e.getFunctionId()))
                    .ifPresent(f -> dto.setFunctionName(f.getName()));
        if (e.getUnidadeOrganicaId() != null)
            organizationalUnitRepository.findById(OrganizationalUnitId.from(e.getUnidadeOrganicaId()))
                    .ifPresent(u -> dto.setUnitName(u.getName()));
        return ResponseEntity.ok(dto);
    }
}
