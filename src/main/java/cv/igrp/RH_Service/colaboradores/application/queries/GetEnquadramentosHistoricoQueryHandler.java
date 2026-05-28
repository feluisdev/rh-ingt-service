package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaEnquadramentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetEnquadramentosHistoricoQueryHandler
        implements QueryHandler<GetEnquadramentosHistoricoQuery, ResponseEntity<WrapperListaEnquadramentoDTO>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final FunctionRepository functionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaEnquadramentoDTO> handle(GetEnquadramentosHistoricoQuery query) {
        var items = enquadramentoRepository.findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId.from(query.getFuncionarioId()));

        Map<UUID, String> careerNames = batchLookup(items.stream().map(EnquadramentoProfissional::getCareerId).collect(Collectors.toSet()),
                id -> careerRepository.findById(CareerId.from(id)).map(c -> c.getName()).orElse(null));
        Map<UUID, String> categoryNames = batchLookup(items.stream().map(EnquadramentoProfissional::getCategoryId).collect(Collectors.toSet()),
                id -> categoryRepository.findById(CategoryId.from(id)).map(c -> c.getName()).orElse(null));
        Map<UUID, String> gradeNames = batchLookup(items.stream().map(EnquadramentoProfissional::getGradeId).collect(Collectors.toSet()),
                id -> gradeRepository.findById(GradeId.from(id)).map(g -> g.getName()).orElse(null));
        Map<UUID, String> cargoNames = batchLookup(items.stream().map(EnquadramentoProfissional::getCargoId).collect(Collectors.toSet()),
                id -> jobRepository.findById(JobId.from(id)).map(j -> j.getName()).orElse(null));
        Map<UUID, String> functionNames = batchLookup(items.stream().map(EnquadramentoProfissional::getFunctionId).collect(Collectors.toSet()),
                id -> functionRepository.findById(FunctionId.from(id)).map(f -> f.getName()).orElse(null));

        Set<UUID> unitIds = items.stream().map(EnquadramentoProfissional::getUnidadeOrganicaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> unitNames = unitIds.isEmpty() ? Map.of() :
                organizationalUnitRepository.findAllByIds(unitIds).stream()
                        .collect(Collectors.toMap(u -> u.getId().getValor(), u -> u.getName()));

        var list = items.stream().map(e -> {
            var dto = mapper.toDTO(e);
            if (e.getCareerId() != null) dto.setCareerName(careerNames.get(e.getCareerId()));
            if (e.getCategoryId() != null) dto.setCategoryName(categoryNames.get(e.getCategoryId()));
            if (e.getGradeId() != null) dto.setGradeName(gradeNames.get(e.getGradeId()));
            if (e.getCargoId() != null) dto.setCargoName(cargoNames.get(e.getCargoId()));
            if (e.getFunctionId() != null) dto.setFunctionName(functionNames.get(e.getFunctionId()));
            if (e.getUnidadeOrganicaId() != null) dto.setUnitName(unitNames.get(e.getUnidadeOrganicaId()));
            return dto;
        }).toList();

        var wrapper = new WrapperListaEnquadramentoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        wrapper.setPageNumber(0);
        wrapper.setPageSize(list.size());
        wrapper.setTotalPages(list.size() == 0 ? 0 : 1);
        wrapper.setFirst(true);
        wrapper.setLast(true);
        return ResponseEntity.ok(wrapper);
    }

    private Map<UUID, String> batchLookup(Set<UUID> ids, java.util.function.Function<UUID, String> loader) {
        return ids.stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(id -> id, loader, (a, b) -> a));
    }
}
