package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.colaboradores.application.dto.ColaboradorDetailsResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.DadosBancariosResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetColaboradorDetailsQueryHandler
        implements QueryHandler<GetColaboradorDetailsQuery, ResponseEntity<ColaboradorDetailsResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final WorkerStateRepository workerStateRepository;
    private final DocumentTypeRepository documentTypeRepository;

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final ContractTypeRepository contractTypeRepository;

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;
    private final GradeRepository gradeRepository;
    private final JobRepository jobRepository;
    private final FunctionRepository functionRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;

    private final DadosBancariosRepository dadosBancariosRepository;
    private final DadosBancariosMapper dadosBancariosMapper;
    private final OptionLookupPort optionLookupPort;

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper documentoMapper;

    @IgrpQueryHandler
    public ResponseEntity<ColaboradorDetailsResponseDTO> handle(GetColaboradorDetailsQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + query.getFuncionarioId()));

        var funcionarioDTO = funcionarioMapper.toDTO(funcionario);
        if (funcionario.getWorkerStateId() != null)
            workerStateRepository.findById(WorkerStateId.from(funcionario.getWorkerStateId()))
                    .ifPresent(ws -> funcionarioDTO.setWorkerStateName(ws.getDescription()));
        if (funcionario.getDocumentTypeId() != null)
            documentTypeRepository.findById(DocumentTypeId.from(funcionario.getDocumentTypeId()))
                    .ifPresent(dt -> funcionarioDTO.setDocumentTypeName(dt.getDescricao()));

        var contratoDTO = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(c -> {
                    var dto = contratoMapper.toDTO(c);
                    if (c.getContractTypeId() != null)
                        contractTypeRepository.findById(ContractTypeId.from(c.getContractTypeId()))
                                .ifPresent(ct -> dto.setContractTypeName(ct.getDescription()));
                    return dto;
                }).orElse(null);

        // Enquadramento derivado do NOVO modelo: afectação corrente + Lugar (Position).
        // A forma da resposta mantém-se por compatibilidade com o frontend.
        var enquadramentoDTO = assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId)
                .map(a -> {
                    var pos = a.getPositionId() != null
                            ? positionRepository.findById(PositionId.from(a.getPositionId())).orElse(null)
                            : null;
                    var dto = new EnquadramentoResponseDTO();
                    dto.setId(a.getId().getStringValor());
                    dto.setFuncionarioId(funcionarioId.getStringValor());
                    dto.setDataInicio(a.getDataInicio());
                    dto.setDataFim(a.getDataFim());
                    dto.setIsCurrent(a.getIsCurrent());
                    dto.setIsCurrentDesc(Boolean.TRUE.equals(a.getIsCurrent()) ? "Corrente" : "Histórico");
                    if (a.getGradeId() != null) {
                        dto.setGradeId(a.getGradeId().toString());
                        gradeRepository.findById(GradeId.from(a.getGradeId()))
                                .ifPresent(g -> dto.setGradeName(g.getName()));
                    }
                    if (a.getFunctionId() != null) {
                        dto.setFunctionId(a.getFunctionId().toString());
                        functionRepository.findById(FunctionId.from(a.getFunctionId()))
                                .ifPresent(f -> dto.setFunctionName(f.getName()));
                    }
                    if (pos != null) {
                        if (pos.getCareerId() != null) {
                            dto.setCareerId(pos.getCareerId().toString());
                            careerRepository.findById(CareerId.from(pos.getCareerId()))
                                    .ifPresent(c -> dto.setCareerName(c.getName()));
                        }
                        if (pos.getCategoryId() != null) {
                            dto.setCategoryId(pos.getCategoryId().toString());
                            categoryRepository.findById(CategoryId.from(pos.getCategoryId()))
                                    .ifPresent(c -> dto.setCategoryName(c.getName()));
                        }
                        if (pos.getJobId() != null) {
                            dto.setCargoId(pos.getJobId().toString());
                            jobRepository.findById(JobId.from(pos.getJobId()))
                                    .ifPresent(j -> dto.setCargoName(j.getName()));
                        }
                        if (pos.getUnidadeOrganicaId() != null) {
                            dto.setUnidadeOrganicaId(pos.getUnidadeOrganicaId().toString());
                            organizationalUnitRepository.findById(OrganizationalUnitId.from(pos.getUnidadeOrganicaId()))
                                    .ifPresent(u -> dto.setUnitName(u.getName()));
                        }
                    }
                    return dto;
                }).orElse(null);

        DadosBancariosResponseDTO dadosBancariosDTO = null;
        var dadosBancariosList = dadosBancariosRepository.findAllByFuncionarioId(funcionarioId)
                .stream().filter(d -> Boolean.TRUE.equals(d.getIsActive())).toList();
        if (!dadosBancariosList.isEmpty()) {
            var db = dadosBancariosList.get(0);
            dadosBancariosDTO = dadosBancariosMapper.toDTO(db);
            if (db.getBanco() != null) {
                Set<String> bancoCkeys = Set.of(db.getBanco());
                Map<String, OptionDTO> bancoMap = optionLookupPort.findAllByCcodeAndCkeys(
                        OptionCcode.BANCO.getCode(), bancoCkeys);
                OptionDTO opt = bancoMap.get(db.getBanco());
                if (opt != null) dadosBancariosDTO.setBancoDesc(opt.cvalue());
            }
        }

        var filter = new DocumentoFilter();
        filter.setActive(true);
        var documentos = documentoRepository
                .findAllByReference("FUNCIONARIO", funcionarioId.getValor(), filter)
                .stream().map(documentoMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(new ColaboradorDetailsResponseDTO(
                funcionarioDTO, contratoDTO, enquadramentoDTO, dadosBancariosDTO, documentos));
    }
}
